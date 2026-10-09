package com.johnai.assistant;

import android.app.*;
import android.os.*;
import android.content.*;
import android.speech.*;
import android.speech.tts.TextToSpeech;
import android.graphics.Color;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.json.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout root, messages;
    EditText input;
    String apiKey = "", model = "gpt-4o-mini";
    TextToSpeech tts;
    final int BLUE = Color.rgb(35, 164, 255);
    final ArrayList<JSONObject> history = new ArrayList<>();

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(7,12,25));
        getWindow().setNavigationBarColor(Color.rgb(7,12,25));
        apiKey = getPreferences(0).getString("api_key", "");
        tts = new TextToSpeech(this, status -> {});
        buildUI();
        addBubble("John AI", "Hey! I'm John AI. Add your API key in Settings to start chatting.");
    }

    TextView text(String s, int size, int color) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        t.setPadding(14, 10, 14, 10); return t;
    }
    GradientDrawable bg(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(radius); return d;
    }
    Button button(String label) {
        Button b = new Button(this); b.setText(label); b.setAllCaps(false); return b;
    }
    void buildUI() {
        root = new LinearLayout(this); root.setOrientation(1); root.setBackgroundColor(Color.rgb(9,15,30));
        LinearLayout top = new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(12,8,12,8); top.setBackgroundColor(Color.rgb(12,22,43));
        TextView title = text("✦  JOHN AI", 21, Color.WHITE); title.setTypeface(null,1);
        top.addView(title, new LinearLayout.LayoutParams(0,-2,1));
        Button settings = button("Settings"); settings.setOnClickListener(v -> settings()); top.addView(settings);
        root.addView(top);
        ScrollView scroll = new ScrollView(this);
        messages = new LinearLayout(this); messages.setPadding(10,10,10,10); messages.setOrientation(1);
        scroll.addView(messages); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        TextView note = text("Online AI • Your API key stays on this device", 12, Color.LTGRAY);
        root.addView(note);
        LinearLayout bar = new LinearLayout(this); bar.setPadding(7,5,7,7); bar.setGravity(Gravity.CENTER_VERTICAL);
        input = new EditText(this); input.setSingleLine(false); input.setMinLines(1); input.setMaxLines(4);
        input.setHint("Message John AI…"); input.setTextColor(Color.WHITE); input.setHintTextColor(Color.GRAY);
        input.setBackground(bg(Color.rgb(23,34,56),28)); input.setPadding(12,8,12,8);
        bar.addView(input,new LinearLayout.LayoutParams(0,-2,1));
        Button mic = button("🎙"); mic.setOnClickListener(v -> voiceInput()); bar.addView(mic);
        Button send = button("➤"); send.setOnClickListener(v -> sendMessage()); bar.addView(send);
        root.addView(bar); setContentView(root);
    }
    void addBubble(String who, String body) {
        TextView t = text(who + "\n" + body, 15, Color.WHITE);
        t.setBackground(bg(who.equals("You") ? Color.rgb(25,69,112) : Color.rgb(25,34,53), 18));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,5,0,5);
        messages.addView(t,p);
    }
    void settings() {
        LinearLayout box = new LinearLayout(this); box.setOrientation(1); box.setPadding(8,4,8,4);
        EditText key = new EditText(this); key.setHint("API key (sk-...)"); key.setSingleLine(true); key.setText(apiKey);
        EditText modelBox = new EditText(this); modelBox.setHint("Model (e.g. gpt-4o-mini)"); modelBox.setSingleLine(true); modelBox.setText(model);
        box.addView(text("Your API key is stored locally on this phone. Never share it.",13,Color.DKGRAY));
        box.addView(key); box.addView(modelBox);
        new AlertDialog.Builder(this).setTitle("John AI Settings").setView(box)
            .setPositiveButton("Save",(d,w)->{
                apiKey=key.getText().toString().trim();
                model=modelBox.getText().toString().trim();
                if(model.isEmpty()) model="gpt-4o-mini";
                getPreferences(0).edit().putString("api_key",apiKey).apply();
                Toast.makeText(this,"Settings saved",Toast.LENGTH_SHORT).show();
            }).setNegativeButton("Cancel",null).show();
    }
    void sendMessage() {
        String q=input.getText().toString().trim(); if(q.isEmpty()) return;
        if(apiKey.isEmpty()) { new AlertDialog.Builder(this).setMessage("Add your API key in Settings first. API usage may cost money.").setPositiveButton("Settings",(d,w)->settings()).setNegativeButton("Cancel",null).show(); return; }
        input.setText(""); ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(input.getWindowToken(),0);
        addBubble("You",q);
        try { JSONObject u=new JSONObject(); u.put("role","user"); u.put("content",q); history.add(u); } catch(Exception ignored){}
        addBubble("John AI","Thinking…");
        final int placeholder = messages.getChildCount()-1;
        new Thread(() -> {
            String answer;
            try { answer=callApi(); }
            catch(Exception e) { answer="Request failed: "+e.getMessage()+"\nCheck your internet, API key, model access, and API billing."; }
            final String result=answer;
            runOnUiThread(() -> {
                if(placeholder>=0 && placeholder<messages.getChildCount()) messages.removeViewAt(placeholder);
                addBubble("John AI",result);
            });
        }).start();
    }
    String callApi() throws Exception {
        URL url=new URL("https://api.openai.com/v1/chat/completions");
        HttpURLConnection c=(HttpURLConnection)url.openConnection();
        c.setRequestMethod("POST"); c.setConnectTimeout(20000); c.setReadTimeout(60000);
        c.setDoOutput(true); c.setRequestProperty("Authorization","Bearer "+apiKey);
        c.setRequestProperty("Content-Type","application/json");
        JSONArray msgs=new JSONArray();
        JSONObject sys=new JSONObject(); sys.put("role","system"); sys.put("content","You are John AI, a helpful, friendly AI assistant. Be clear and honest."); msgs.put(sys);
        for(JSONObject m:history) msgs.put(m);
        JSONObject payload=new JSONObject(); payload.put("model",model); payload.put("messages",msgs); payload.put("temperature",0.7);
        try(OutputStream os=c.getOutputStream()){ os.write(payload.toString().getBytes("UTF-8")); }
        int code=c.getResponseCode(); InputStream is=code<400?c.getInputStream():c.getErrorStream();
        ByteArrayOutputStream out=new ByteArrayOutputStream(); byte[] buf=new byte[4096]; int n;
        while((n=is.read(buf))!=-1) out.write(buf,0,n);
        String response=out.toString("UTF-8"); c.disconnect();
        JSONObject data=new JSONObject(response);
        if(code>=400) throw new Exception(data.optJSONObject("error")!=null?data.getJSONObject("error").optString("message","HTTP "+code):"HTTP "+code);
        String answer=data.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
        JSONObject a=new JSONObject(); a.put("role","assistant"); a.put("content",answer); history.add(a);
        return answer;
    }
    void voiceInput() {
        try {
            Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to John AI");
            startActivityForResult(i,101);
        } catch(Exception e) { Toast.makeText(this,"Speech recognition isn't available on this device.",Toast.LENGTH_LONG).show(); }
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==101 && resultCode==RESULT_OK && data!=null) {
            ArrayList<String> r=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(r!=null&&!r.isEmpty()) { input.setText(r.get(0)); input.setSelection(input.length()); }
        }
    }
    @Override public void onDestroy() { if(tts!=null) tts.shutdown(); super.onDestroy(); }
}
