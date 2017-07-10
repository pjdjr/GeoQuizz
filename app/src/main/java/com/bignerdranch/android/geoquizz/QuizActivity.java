package com.bignerdranch.android.geoquizz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.math.BigDecimal;
import java.util.Arrays;

public class QuizActivity extends AppCompatActivity {

    private static final String TAG = "QuizActivity";
    private static final String KEY_INDEX = "index";
    private static final String KEY_ALREADY_ANSWERED = "already answered";
    private static final String KEY_RIGHT_OR_WRONG = "right or wrong";
    private static final int REQUEST_CODE_CHEAT = 0;
    private static final String EXTRA_ANSWER_IS_TRUE = "com.bignerdranch.android.geoquizz.answer_is_true";
    private static final String KEY_DID_THEY_CHEAT = "did they cheat";

    private Button mTrueButton;
    private Button mFalseButton;
    private ImageButton mNextButton;
    private ImageButton mPreviousButton;
    private TextView mQuestionTextView;
    private Button mCheatButton;
    private TextView mCheatsRemaining;
 //   private boolean mIsCheater;

    public static Intent newIntent(Context packageContext, boolean answerIsTrue) {
        Intent intent = new Intent(packageContext, CheatActivity.class);
        intent.putExtra(EXTRA_ANSWER_IS_TRUE, answerIsTrue);
        return intent;
    }

    private Question[] mQuestionBank = new Question[] {
        new Question(R.string.question_australia, true),
        new Question(R.string.question_oceans, true),
        new Question(R.string.question_mideast, false),
        new Question(R.string.question_africa, false),
        new Question(R.string.question_americas, true),
        new Question(R.string.question_asia, true),
    };

    private int mCurrentIndex = 0;
    private boolean alreadyAnswered[] = new boolean[mQuestionBank.length]; //defaults all entries to false
    private int rightOrWrong[] = new int[mQuestionBank.length]; //defaults all entries to false
    private boolean didTheyCheat[] = new boolean[mQuestionBank.length]; //defaults all entries to false
    private int cheatsRemaining;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(Bundle) called");
        setContentView(R.layout.activity_quiz);

        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
            alreadyAnswered = savedInstanceState.getBooleanArray(KEY_ALREADY_ANSWERED);
            rightOrWrong = savedInstanceState.getIntArray(KEY_RIGHT_OR_WRONG);
            didTheyCheat = savedInstanceState.getBooleanArray(KEY_DID_THEY_CHEAT);
            Log.d(TAG, "mCurrentIndex = " + mCurrentIndex);
            Log.d(TAG, "didTheyCheat[] = " + Arrays.toString(didTheyCheat));
        }

        mQuestionTextView = (TextView) findViewById(R.id.question_text_view);
        mQuestionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
            updateQuestion();
            }
        });

        mTrueButton = (Button) findViewById(R.id.true_button);
        mTrueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            checkAnswer(true);
            setButtonState();
            }
        });
        mFalseButton = (Button) findViewById(R.id.false_button);
        mFalseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            checkAnswer(false);
            setButtonState();
            }
        });
        mNextButton = (ImageButton) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
            updateQuestion();
            Log.d(TAG, "mCurrentIndex = " + mCurrentIndex);
            Log.d(TAG, "didTheyCheat[] = " + Arrays.toString(didTheyCheat));
            }
        });

        mPreviousButton = (ImageButton) findViewById(R.id.previous_button);
        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            mCurrentIndex = (mQuestionBank.length + mCurrentIndex - 1) % mQuestionBank.length;
            updateQuestion();
            Log.d(TAG, "mCurrentIndex = " + mCurrentIndex);
            Log.d(TAG, "didTheyCheat[] = " + Arrays.toString(didTheyCheat));
            }
        });

        mCheatButton = (Button)findViewById(R.id.cheat_button);
        mCheatButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //Start CheatActivity
                boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
                Intent intent = CheatActivity.newIntent(QuizActivity.this, answerIsTrue);
                startActivityForResult(intent, REQUEST_CODE_CHEAT);
            }
        });

        setCheatsRemaining();

        updateQuestion();
    }

    private void updateQuestion() {
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);
        setButtonState();
    }

    private void checkAnswer(boolean userPressedTrue) {
        Log.d(TAG, "in userPressedTrue()");
        alreadyAnswered[mCurrentIndex] = true;
        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
        int messageResId;
        if (didTheyCheat[mCurrentIndex]) {
            messageResId = R.string.judgment_toast;
        } else {
            if (userPressedTrue == answerIsTrue) {
                messageResId = R.string.correct_toast;
            } else {
                messageResId = R.string.incorrect_toast;
            }
        }
        recordAnswer(messageResId);
    }

    private void setButtonState() {
        if (alreadyAnswered[mCurrentIndex] == true) {
            mTrueButton.setEnabled(false);
            mFalseButton.setEnabled(false);
        } else if (alreadyAnswered[mCurrentIndex] == false) {
            mTrueButton.setEnabled(true);
            mFalseButton.setEnabled(true);
        }
        setCheatsRemaining();
    }

    private void recordAnswer(int messageResId) {
        Log.d(TAG, "in recordAnswer()");
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
        if (messageResId == R.string.correct_toast) {
            rightOrWrong[mCurrentIndex] = 1; //1 is correct, -1 is incorrect, default is 0
        } else if (messageResId == R.string.incorrect_toast) {
            rightOrWrong[mCurrentIndex] = -1; //1 is correct, -1 is incorrect, default is 0
        } else if (messageResId == R.string.judgment_toast) {
            rightOrWrong[mCurrentIndex] = -1; //1 is correct, -1 is incorrect, default is 0; cheating counts as incorrect
        }
        tallyScore();
    }

    private void tallyScore() {
        Log.d(TAG, "in tallyScore()");
        int i = 0;
        boolean allAnswered = true;
        while (i < alreadyAnswered.length && allAnswered == true) {
            Log.d(TAG, "in while loop");
            if (alreadyAnswered[i] == true) {
                i++;
            } else {
                allAnswered = false;
            }
        }
        double numberRight = 0;
        if (allAnswered == true) {
            for(int j = 0; j < rightOrWrong.length; j++) {
                if (rightOrWrong[j] == 1) {
                    numberRight++;
                }
            }
            double score = numberRight/(rightOrWrong.length)*100;
            String stringScore = String.valueOf(round(score, 2));
            Context context = getApplicationContext();
            CharSequence text = "Your score was " + stringScore + "%.";
            if (score > 75) {
                text = "Good job! " + text;
            }
            Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200);
            toast.show();
            Arrays.fill(alreadyAnswered, false);
            Arrays.fill(rightOrWrong, 0);
            Arrays.fill(didTheyCheat, false);
            setCheatsRemaining();
            Toast playAgainToast = Toast.makeText(context, "Let's play again!", Toast.LENGTH_SHORT);
            playAgainToast.setGravity(Gravity.CENTER_VERTICAL, 0, -200);
            playAgainToast.show();
        }
    }

    public static double round(double d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    private void setCheatsRemaining(){
        int cheatCount = 0;
        for(int i = 0; i < didTheyCheat.length; i++) {
            if (didTheyCheat[i]) {
                cheatCount++;
            }
        };
        cheatsRemaining = 3 - cheatCount;
        Log.d(TAG, String.valueOf((cheatsRemaining) + " Cheats Remaining"));
        mCheatsRemaining = (TextView) findViewById(R.id.cheats_remaining);
        String cheatMessage = createCheatMessage(cheatsRemaining);
        mCheatsRemaining.setText(cheatMessage);
        if (cheatsRemaining == 0 || didTheyCheat[mCurrentIndex] || alreadyAnswered[mCurrentIndex]){
            mCheatButton.setEnabled(false);
        } else {
            mCheatButton.setEnabled(true);
        }
    }

    private String createCheatMessage(int cheatsRemaining) {
        String returnString = (cheatsRemaining == 1)
                            ? (String.valueOf((cheatsRemaining) + " Cheat Remaining"))
                            : (String.valueOf((cheatsRemaining) + " Cheats Remaining"));
        return returnString;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            Log.d(TAG, "Result code != Activity.RESULT_OK " + resultCode);
            return;
        }
        if (requestCode == REQUEST_CODE_CHEAT) {
            if (data == null) {
                Log.d(TAG, "data == null");
                return;
            }

            Log.d(TAG, "requestCode == REQUEST_CODE_CHEAT " + resultCode);
            didTheyCheat[mCurrentIndex] = CheatActivity.wasAnswerShown(data);;
            Log.d(TAG, "mCurrentIndex = " + mCurrentIndex);
            Log.d(TAG, "didTheyCheat[] = " + Arrays.toString(didTheyCheat));
            setCheatsRemaining();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState");
        savedInstanceState.putInt(KEY_INDEX, mCurrentIndex);
        savedInstanceState.putBooleanArray(KEY_ALREADY_ANSWERED, alreadyAnswered);
        savedInstanceState.putIntArray(KEY_RIGHT_OR_WRONG, rightOrWrong);
        savedInstanceState.putBooleanArray(KEY_DID_THEY_CHEAT, didTheyCheat);
        setCheatsRemaining();
    }

}
