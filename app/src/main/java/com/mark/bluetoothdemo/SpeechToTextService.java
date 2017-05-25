package com.mark.bluetoothdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;


/**
 * Created on 2017/5/4
 *
 * @author Mark Hsu
 */

class SpeechToTextService {
    private static final String TAG = "SpeechToTextService";
    private SpeechRecognizer speechRecognizer;
    private Handler mHandler;
    private Context mContext;

    SpeechToTextService(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
        initialize();
    }

    void initialize() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext);
        speechRecognizer.setRecognitionListener(new STTServiceListener());
    }

    void start() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        speechRecognizer.startListening(intent);
    }

    private class STTServiceListener implements RecognitionListener {

        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "onReadyForSpeech");
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech");
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            Log.d(TAG, "onRmsChanged");
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "onBufferReceived");
        }

        @Override
        public void onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech");
        }


        /**
         * Network operation timed out.
         * public static final int ERROR_NETWORK_TIMEOUT = 1;
         * <p>
         * Other network related errors.
         * public static final int ERROR_NETWORK = 2;
         * <p>
         * Audio recording error.
         * public static final int ERROR_AUDIO = 3;
         * <p>
         * Server sends error status.
         * public static final int ERROR_SERVER = 4;
         * <p>
         * Other client side errors.
         * public static final int ERROR_CLIENT = 5;
         * <p>
         * No speech input
         * public static final int ERROR_SPEECH_TIMEOUT = 6;
         * <p>
         * No recognition result matched.
         * public static final int ERROR_NO_MATCH = 7;
         * <p>
         * RecognitionService busy.
         * public static final int ERROR_RECOGNIZER_BUSY = 8;
         * <p>
         * Insufficient permissions
         * public static final int ERROR_INSUFFICIENT_PERMISSIONS = 9;
         */
        @Override
        public void onError(int error) {
            Log.e(TAG, "onError: " + error);
            speechRecognizer.cancel();
            String message;
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = mContext.getResources().getString(R.string.stt_audio_error);
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = mContext.getResources().getString(R.string.stt_client_error);
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = mContext.getResources().getString(R.string.stt_permission_error);
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = mContext.getResources().getString(R.string.stt_network_error);
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = mContext.getResources().getString(R.string.stt_network_timeout);
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = mContext.getResources().getString(R.string.stt_server_error);
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = mContext.getResources().getString(R.string.stt_service_busy);
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = mContext.getResources().getString(R.string.stt_no_matched);
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = mContext.getResources().getString(R.string.stt_speech_timeout);
                    break;
                default:
                    message = mContext.getResources().getString(R.string.stt_unknown_error);
                    break;
            }
            Log.e(TAG, "Error message: " + message);
            mHandler.obtainMessage(Constants.STT_ERROR, message).sendToTarget();
        }

        @Override
        public void onResults(Bundle results) {
            Log.d(TAG, "onResults " + results);

            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (data != null) {
                mHandler.obtainMessage(Constants.STT_RESULT_RECOGNITION, data.toString()).sendToTarget();
                for (int i = 0; i < data.size(); i++) {
                    Log.d(TAG, "data.get(" + i + "): " + data.get(i));
                    switch (data.get(i).toString()) {
                        case "位置":
                        case "位子":
                            mHandler.sendEmptyMessage(Constants.STT_ASK_LOCATION);
                            return;

                        case "施工":
                        case "障礙":
                            mHandler.sendEmptyMessage(Constants.STT_ASK_OBSTACLE);
                            return;
                    }
                }
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            Log.d(TAG, "onPartialResults");
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent " + eventType);
        }
    }
}
