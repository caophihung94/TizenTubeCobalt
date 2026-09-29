package dev.cobalt.coat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class VoiceSearchActivity extends Activity {
    private SpeechRecognizer speechRecognizer;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        statusText = new TextView(this);
        statusText.setText("🎙️ Đang nghe...");
        statusText.setTextSize(24);
        statusText.setTextColor(Color.WHITE);
        statusText.setBackgroundColor(Color.parseColor("#CC000000"));
        statusText.setGravity(Gravity.CENTER);
        setContentView(statusText);

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            return; 
        }
        
        startListening();
    }

    private void startListening() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    statusText.setText("🎙️ Đang nghe, hãy nói nội dung cần tìm...");
                }

                @Override
                public void onBeginningOfSpeech() {}
                @Override
                public void onRmsChanged(float rmsdB) {}
                @Override
                public void onBufferReceived(byte[] buffer) {}
                
                @Override
                public void onEndOfSpeech() {
                    statusText.setText("⏳ Đang xử lý...");
                }

                @Override
                public void onError(int error) {
                    Toast.makeText(VoiceSearchActivity.this, "Không nhận diện được giọng nói (Lỗi: " + error + ")", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String query = matches.get(0);
                        
                        String url = "https://www.youtube.com/tv#/search?q=" + Uri.encode(query);
                        
                        Intent searchIntent = new Intent();
                        searchIntent.setClassName(getPackageName(), "dev.cobalt.app.MainActivity");
                        searchIntent.setAction(Intent.ACTION_VIEW);
                        searchIntent.setData(Uri.parse(url));
                        
                        searchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(searchIntent);
                    }
                    finish();
                }

                @Override
                public void onPartialResults(Bundle partialResults) {}
                @Override
                public void onEvent(int eventType, Bundle params) {}
            });

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechRecognizer.startListening(intent);
        } else {
            Toast.makeText(this, "TV của bạn không có dịch vụ SpeechRecognizer ngầm!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startListening();
        } else {
            Toast.makeText(this, "Cần quyền Micro để Voice Search hoạt động!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}
