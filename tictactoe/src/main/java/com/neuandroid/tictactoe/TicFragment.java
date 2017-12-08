/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.neuandroid.tictactoe;

import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;


public class TicFragment extends Fragment {

    /** Start player. Must be 1 or 2. Default is 1. */
    public static final String EXTRA_START_PLAYER =
        "com.neuandroid.tictactoe.TicFragment.EXTRA_START_PLAYER";

    private static final int MSG_COMPUTER_TURN = 1;
    private static final long COMPUTER_DELAY_MS = 500;

    private Handler mHandler = new Handler(new MyHandlerCallback());
    private Random mRnd = new Random();
    private TicBoardView mTicBoardView;
    private TextView mInfoView;
    private Button mButtonNext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.lib_game, container, false);
        mTicBoardView = view.findViewById(R.id.game_view);
        mInfoView = view.findViewById(R.id.info_turn);
        mButtonNext = view.findViewById(R.id.next_turn);

        mTicBoardView.setFocusable(true);
        mTicBoardView.setFocusableInTouchMode(true);
        mTicBoardView.setCellListener(new MyCellListener());

        mButtonNext.setOnClickListener(new MyButtonListener());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        TicBoardView.State player = mTicBoardView.getCurrentPlayer();
        if (player == TicBoardView.State.UNKNOWN) {
//            player = TicBoardView.State.fromInt(getIntent().getIntExtra(EXTRA_START_PLAYER, 1));
            player = TicBoardView.State.PLAYER1;
            if (!checkGameFinished(player)) {
                selectTurn(player);
            }
        }
        if (player == TicBoardView.State.PLAYER2) {
            mHandler.sendEmptyMessageDelayed(MSG_COMPUTER_TURN, COMPUTER_DELAY_MS);
        }
        if (player == TicBoardView.State.WIN) {
            setWinState(mTicBoardView.getWinner());
        }
    }


    private TicBoardView.State selectTurn(TicBoardView.State player) {
        mTicBoardView.setCurrentPlayer(player);
        mButtonNext.setEnabled(false);

        if (player == TicBoardView.State.PLAYER1) {
            mInfoView.setText(R.string.player1_turn);
            mTicBoardView.setEnabled(true);

        } else if (player == TicBoardView.State.PLAYER2) {
            mInfoView.setText(R.string.player2_turn);
            mTicBoardView.setEnabled(false);
        }

        return player;
    }

    private class MyCellListener implements TicBoardView.ICellListener {
        public void onCellSelected() {
            if (mTicBoardView.getCurrentPlayer() == TicBoardView.State.PLAYER1) {
                int cell = mTicBoardView.getSelection();
                mButtonNext.setEnabled(cell >= 0);
            }
        }
    }

    private class MyButtonListener implements OnClickListener {

        public void onClick(View v) {
            TicBoardView.State player = mTicBoardView.getCurrentPlayer();

            if (player == TicBoardView.State.WIN) {

            } else if (player == TicBoardView.State.PLAYER1) {
                int cell = mTicBoardView.getSelection();
                if (cell >= 0) {
                    mTicBoardView.stopBlink();
                    mTicBoardView.setCell(cell, player);
                    finishTurn();
                }
            }
        }
    }

    private class MyHandlerCallback implements Callback {
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_COMPUTER_TURN) {

                // Pick a non-used cell at random. That's about all the AI you need for this game.
                TicBoardView.State[] data = mTicBoardView.getData();
                int used = 0;
                while (used != 0x1F) {
                    int index = mRnd.nextInt(9);
                    if (((used >> index) & 1) == 0) {
                        used |= 1 << index;
                        if (data[index] == TicBoardView.State.EMPTY) {
                            mTicBoardView.setCell(index, mTicBoardView.getCurrentPlayer());
                            break;
                        }
                    }
                }

                finishTurn();
                return true;
            }
            return false;
        }
    }

    private TicBoardView.State getOtherPlayer(TicBoardView.State player) {
        return player == TicBoardView.State.PLAYER1 ? TicBoardView.State.PLAYER2 : TicBoardView.State.PLAYER1;
    }

    private void finishTurn() {
        TicBoardView.State player = mTicBoardView.getCurrentPlayer();
        if (!checkGameFinished(player)) {
            player = selectTurn(getOtherPlayer(player));
            if (player == TicBoardView.State.PLAYER2) {
                mHandler.sendEmptyMessageDelayed(MSG_COMPUTER_TURN, COMPUTER_DELAY_MS);
            }
        }
    }

    public boolean checkGameFinished(TicBoardView.State player) {
        TicBoardView.State[] data = mTicBoardView.getData();
        boolean full = true;

        int col = -1;
        int row = -1;
        int diag = -1;

        // check rows
        for (int j = 0, k = 0; j < 3; j++, k += 3) {
            if (data[k] != TicBoardView.State.EMPTY && data[k] == data[k+1] && data[k] == data[k+2]) {
                row = j;
            }
            if (full && (data[k] == TicBoardView.State.EMPTY ||
                         data[k+1] == TicBoardView.State.EMPTY ||
                         data[k+2] == TicBoardView.State.EMPTY)) {
                full = false;
            }
        }

        // check columns
        for (int i = 0; i < 3; i++) {
            if (data[i] != TicBoardView.State.EMPTY && data[i] == data[i+3] && data[i] == data[i+6]) {
                col = i;
            }
        }

        // check diagonals
        if (data[0] != TicBoardView.State.EMPTY && data[0] == data[1+3] && data[0] == data[2+6]) {
            diag = 0;
        } else  if (data[2] != TicBoardView.State.EMPTY && data[2] == data[1+3] && data[2] == data[0+6]) {
            diag = 1;
        }

        if (col != -1 || row != -1 || diag != -1) {
            setFinished(player, col, row, diag);
            return true;
        }

        // if we get here, there's no winner but the board is full.
        if (full) {
            setFinished(TicBoardView.State.EMPTY, -1, -1, -1);
            return true;
        }
        return false;
    }

    private void setFinished(TicBoardView.State player, int col, int row, int diagonal) {

        mTicBoardView.setCurrentPlayer(TicBoardView.State.WIN);
        mTicBoardView.setWinner(player);
        mTicBoardView.setEnabled(false);
        mTicBoardView.setFinished(col, row, diagonal);

        setWinState(player);
    }

    private void setWinState(TicBoardView.State player) {
        mButtonNext.setEnabled(true);
        mButtonNext.setText("Back");

        String text;

        if (player == TicBoardView.State.EMPTY) {
            text = getString(R.string.tie);
        } else if (player == TicBoardView.State.PLAYER1) {
            text = getString(R.string.player1_win);
        } else {
            text = getString(R.string.player2_win);
        }
        mInfoView.setText(text);
    }
}
