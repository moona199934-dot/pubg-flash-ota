package com.pubgflash.gamebooster;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.*;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import android.app.ActivityManager;
import java.io.*;
import java.net.*;
import java.util.*;
import android.content.ContentValues;
import android.database.Cursor;

public class MainActivity extends Activity {

    private final int BG=0xFF070B14;
    private final int CARD=0xFF111B2D;
    private final int BLUE=0xFF16D9FF;
    private final int PURPLE=0xFF9C65FF;
    private final int WHITE=0xFFF3F7FF;
    private final int GRAY=0xFF91A3BA;
    private final String GAME="com.tencent.igfit";

    private LinearLayout root,content;
    private TextView ramText,tempText,pingText,refreshText;
    private Handler handler=new Handler();
    private android.content.SharedPreferences prefs;
    private String page="HOME";
    private String lastPing="Ölçülmedi";
    private String lastLog="V6 başlatıldı";
    private boolean purpleTheme=false;

    private final String[] names={
        "TPP Kamera","FPP Kamera","Red Dot","2x Scope",
        "3x Scope","4x Scope","6x Scope","8x Scope",
        "TPP ADS","FPP ADS","Red Dot ADS","2x ADS",
        "3x ADS","4x ADS","6x ADS","8x ADS",
        "Jiroskop","Red Dot Jiroskop","2x Jiroskop",
        "3x Jiroskop","4x Jiroskop","6x Jiroskop","8x Jiroskop"
    };

    private final int[] defaults={
        120,110,50,35,25,20,12,10,
        110,100,45,32,24,18,10,8,
        0,0,0,0,0,0,0
    };

    private final Runnable ticker=new Runnable(){
        @Override public void run(){
            refreshMetrics();
            handler.postDelayed(this,3000);
        }
    };

    private int accent(){
        return purpleTheme?PURPLE:BLUE;
    }

    private GradientDrawable bg(int color,int radius){
        GradientDrawable d=new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        d.setStroke(1,0xFF263B5A);
        return d;
    }

    private TextView text(String value,int size,int color){
        TextView t=new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return t;
    }

    private void gap(int height){
        Space s=new Space(this);
        content.addView(s,new LinearLayout.LayoutParams(1,height));
    }

    private void log(String message){
        String line="["+new java.text.SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",Locale.getDefault()
        ).format(new Date())+"] "+message;

        lastLog=line;

        try{
            File f=new File(getFilesDir(),"activity.log");
            FileWriter w=new FileWriter(f,true);
            w.write(line+"\n");
            w.close();
        }catch(Exception ignored){}

        // Android 10+ Download klasörüne uygulama günlüğü
        if(Build.VERSION.SDK_INT>=29){
            try{
                Uri collection=MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                Uri fileUri=null;

                Cursor c=getContentResolver().query(
                    collection,
                    new String[]{MediaStore.Downloads._ID},
                    MediaStore.Downloads.DISPLAY_NAME+"=?",
                    new String[]{"PUBG_FLASH_AI_BOOSTER.log"},
                    null
                );

                if(c!=null){
                    if(c.moveToFirst()){
                        fileUri=ContentUris.withAppendedId(
                            collection,c.getLong(0));
                    }
                    c.close();
                }

                if(fileUri==null){
                    ContentValues values=new ContentValues();
                    values.put(MediaStore.Downloads.DISPLAY_NAME,
                        "PUBG_FLASH_AI_BOOSTER.log");
                    values.put(MediaStore.Downloads.MIME_TYPE,
                        "text/plain");
                    values.put(MediaStore.Downloads.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS+"/PROJE_KAYITLARI");
                    fileUri=getContentResolver().insert(collection,values);
                }

                if(fileUri!=null){
                    OutputStream out=getContentResolver()
                        .openOutputStream(fileUri,"wa");
                    if(out!=null){
                        out.write((line+"\n").getBytes("UTF-8"));
                        out.close();
                    }
                }
            }catch(Exception ignored){}
        }
    }

    private void init(){
        root=new LinearLayout(this);
        root.setOrientation(1);
        root.setBackgroundColor(BG);

        LinearLayout header=new LinearLayout(this);
        header.setPadding(18,17,18,12);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(BG);

        TextView brand=text("FLASH // AI",20,accent());
        header.addView(brand,new LinearLayout.LayoutParams(0,-2,1));

        TextView version=text("V6.0  ● ONLINE",10,0xFF45E6A0);
        header.addView(version);
        root.addView(header);

        ScrollView scroll=new ScrollView(this);
        content=new LinearLayout(this);
        content.setOrientation(1);
        content.setPadding(16,8,16,20);
        scroll.addView(content);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout nav=new LinearLayout(this);
        nav.setPadding(5,8,5,8);
        nav.setBackgroundColor(0xFF0D1523);

        String[] labels={"HOME","AI","PERF","AIM","TOOLS","LOG"};
        String[] pages={"HOME","AI","PERF","AIM","TOOLS","LOG"};

        for(int i=0;i<labels.length;i++){
            final String p=pages[i];
            TextView b=text(labels[i],10,p.equals(page)?accent():GRAY);
            b.setGravity(Gravity.CENTER);
            b.setPadding(2,12,2,12);
            nav.addView(b,new LinearLayout.LayoutParams(0,-2,1));
            b.setOnClickListener(v->showPage(p));
        }

        root.addView(nav);
        setContentView(root);
    }

    private void card(String title,String value,String detail){
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(1);
        box.setPadding(16,15,16,15);
        box.setBackground(bg(CARD,20));

        TextView a=text(title,12,GRAY);
        TextView b=text(value,23,accent());
        TextView c=text(detail,11,GRAY);

        box.addView(a);
        addSpace(box,8);
        box.addView(b);
        addSpace(box,5);
        box.addView(c);

        LinearLayout.LayoutParams lp=
            new LinearLayout.LayoutParams(-1,-2);
        lp.setMargins(0,5,0,7);
        content.addView(box,lp);
    }

    private void addSpace(LinearLayout l,int h){
        Space s=new Space(this);
        l.addView(s,new LinearLayout.LayoutParams(1,h));
    }

    private void button(String title,Runnable action){
        TextView b=text(title,14,WHITE);
        b.setGravity(Gravity.CENTER);
        b.setPadding(12,16,12,16);
        b.setBackground(bg(0xFF172B47,15));
        LinearLayout.LayoutParams lp=
            new LinearLayout.LayoutParams(-1,-2);
        lp.setMargins(0,5,0,7);
        content.addView(b,lp);
        b.setOnClickListener(v->action.run());
    }

    private void heading(String title){
        TextView t=text(title,16,accent());
        t.setPadding(2,17,2,12);
        content.addView(t);
    }

    private long ramAvailable(){
        ActivityManager.MemoryInfo m=new ActivityManager.MemoryInfo();
        ((ActivityManager)getSystemService(ACTIVITY_SERVICE))
            .getMemoryInfo(m);
        return m.availMem/1024/1024;
    }

    private long ramTotal(){
        ActivityManager.MemoryInfo m=new ActivityManager.MemoryInfo();
        ((ActivityManager)getSystemService(ACTIVITY_SERVICE))
            .getMemoryInfo(m);
        return m.totalMem/1024/1024;
    }

    private String temperature(){
        try{
            return new com.pubgflash.gamebooster.monitor
                .CpuThermalMonitor(this).getCpuTemperature();
        }catch(Exception e){
            return "Sensör bilgisi yok";
        }
    }

    private void refreshMetrics(){
        long available=ramAvailable();
        long total=ramTotal();
        long used=total-available;

        if(ramText!=null)
            ramText.setText(used+" MB / "+total+" MB");

        if(tempText!=null)
            tempText.setText(temperature());

        if(pingText!=null)
            pingText.setText(lastPing);

        if(refreshText!=null){
            float hz=getWindowManager().getDefaultDisplay()
                .getRefreshRate();
            refreshText.setText(String.format(Locale.US,"%.0f Hz",hz));
        }
    }

    private boolean gameInstalled(){
        try{
            return getPackageManager().getLaunchIntentForPackage(GAME)!=null;
        }catch(Exception e){
            return false;
        }
    }

    private void launchGame(){
        Intent i=getPackageManager().getLaunchIntentForPackage(GAME);
        if(i==null){
            Toast.makeText(this,
                "PUBG Flash bulunamadı. Paket: "+GAME,
                Toast.LENGTH_LONG).show();
            log("PUBG Flash bulunamadı");
            return;
        }
        log("PUBG Flash başlatıldı");
        startActivity(i);
    }

    private String aiAdvice(){
        ActivityManager.MemoryInfo m=new ActivityManager.MemoryInfo();
        ((ActivityManager)getSystemService(ACTIVITY_SERVICE))
            .getMemoryInfo(m);

        long free=m.availMem/1024/1024;
        long total=m.totalMem/1024/1024;
        int percent=(int)(free*100/Math.max(1,total));

        String thermal=new com.pubgflash.gamebooster.monitor
            .CpuThermalMonitor(this).getThermalStatus();

        if(percent<15)
            return "Bellek seviyesi düşük. Kullanmadığın uygulamaları kapat.";
        if(thermal.contains("Yüksek")||thermal.contains("Kritik"))
            return "Cihaz sıcak. Soğuması için oyuna ara vermen önerilir.";
        if(percent<30)
            return "Bellek kullanımı yükseliyor. Arka plan yükünü kontrol et.";
        return "Sistem durumu dengeli. PUBG Flash için hazır görünüyorsun.";
    }

    private void showPage(String p){
        page=p;
        content.removeAllViews();

        switch(p){
            case "HOME": home(); break;
            case "AI": aiPage(); break;
            case "PERF": performance(); break;
            case "AIM": sensitivity(); break;
            case "TOOLS": tools(); break;
            case "LOG": history(); break;
        }
    }

    private void home(){
        heading("NEURAL GAMING CONTROL");

        card("SYSTEM STATUS",
            gameInstalled()?"PUBG FLASH READY":"GAME NOT DETECTED",
            "Target package: "+GAME);

        heading("LIVE DEVICE METRICS");

        ramText=text("--",22,accent());
        metric("RAM USAGE",ramText);

        tempText=text("--",22,accent());
        metric("CPU TEMPERATURE",tempText);

        refreshText=text("--",22,accent());
        metric("DISPLAY REFRESH RATE",refreshText);

        pingText=text(lastPing,22,accent());
        metric("NETWORK LATENCY",pingText);

        heading("AI PERFORMANCE ASSISTANT");
        card("AI ANALYSIS",aiAdvice(),
            "Gerçek cihaz ölçümlerine dayalı öneri");

        button("▶  LAUNCH PUBG FLASH",()->launchGame());
        button("✦  OPEN AI ANALYST",()->showPage("AI"));
        button("◈  PERFORMANCE CENTER",()->showPage("PERF"));
        button("⚙  SENSITIVITY LAB",()->showPage("AIM"));

        refreshMetrics();
    }

    private void metric(String name,TextView value){
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(1);
        box.setPadding(15,12,15,12);
        box.setBackground(bg(CARD,15));
        box.addView(text(name,11,GRAY));
        addSpace(box,7);
        box.addView(value);
        LinearLayout.LayoutParams lp=
            new LinearLayout.LayoutParams(-1,-2);
        lp.setMargins(0,4,0,6);
        content.addView(box,lp);
    }

    private void aiPage(){
        heading("✦ FLASH AI ANALYST");
        card("NEURAL STATUS","ACTIVE",
            "Yerel cihaz analizi - internet üzerinden AI iddiası yok");
        card("PERFORMANCE REPORT",aiAdvice(),
            "RAM ve termal durumuna göre hesaplandı");
        card("RAM AVAILABLE",ramAvailable()+" MB",
            "Android ActivityManager ölçümü");
        card("THERMAL STATUS",
            new com.pubgflash.gamebooster.monitor
                .CpuThermalMonitor(this).getThermalStatus(),
            temperature());
        card("GAME DETECTION",
            gameInstalled()?"DETECTED":"NOT FOUND",
            GAME);
        button("⟳  ANALİZİ YENİLE",()->{
            log("AI sistem analizi yenilendi");
            showPage("AI");
        });
        button("▶  PUBG FLASH BAŞLAT",()->launchGame());
    }

    private void performance(){
        heading("PERFORMANCE CENTER");

        ramText=text("--",22,accent());
        metric("RAM USAGE",ramText);

        tempText=text("--",22,accent());
        metric("CPU TEMPERATURE",tempText);

        refreshText=text("--",22,accent());
        metric("DISPLAY REFRESH RATE",refreshText);

        pingText=text(lastPing,22,accent());
        metric("NETWORK LATENCY",pingText);

        card("THERMAL ANALYSIS",
            new com.pubgflash.gamebooster.monitor
                .CpuThermalMonitor(this).getThermalStatus(),
            "Android thermal status");

        card("FPS MONITOR",
            "UI FRAME RATE",
            "Bu uygulamanın ölçümü PUBG oyun içi FPS değeri değildir.");

        button("PING TESTİ YAP",()->measurePing());
        button("ÖLÇÜMLERİ YENİLE",()->{
            refreshMetrics();
            log("Performans ölçümleri yenilendi");
            Toast.makeText(this,"Ölçümler güncellendi",
                Toast.LENGTH_SHORT).show();
        });

        refreshMetrics();
    }

    private void measurePing(){
        lastPing="Ölçülüyor...";
        if(pingText!=null)pingText.setText(lastPing);

        new Thread(()->{
            long start=System.nanoTime();
            String result;
            try(Socket s=new Socket()){
                s.connect(new InetSocketAddress("1.1.1.1",443),3000);
                result=((System.nanoTime()-start)/1000000)+" ms";
            }catch(Exception e){
                result="Bağlantı başarısız";
            }

            final String r=result;
            runOnUiThread(()->{
                lastPing=r;
                if(pingText!=null)pingText.setText(r);
                log("Ping testi: "+r);
            });
        }).start();
    }

    private int sensitivityValue(int i){
        return prefs.getInt("s"+i,defaults[i]);
    }

    private void sensitivity(){
        heading("SENSITIVITY LAB");
        card("ACTIVE PROFILE","CUSTOM",
            "Ayarlar yalnızca booster profilinde saklanır; oyun dosyaları değiştirilmez.");

        for(int i=0;i<names.length;i++){
            final int index=i;
            LinearLayout box=new LinearLayout(this);
            box.setOrientation(1);
            box.setPadding(13,10,13,10);
            box.setBackground(bg(CARD,14));

            TextView label=text(names[i]+"  :  "+
                sensitivityValue(i),13,WHITE);
            box.addView(label);

            SeekBar seek=new SeekBar(this);
            seek.setMax(400);
            seek.setProgress(sensitivityValue(i));
            box.addView(seek);

            seek.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener(){
                    public void onProgressChanged(
                        SeekBar b,int value,boolean user){
                        label.setText(names[index]+"  :  "+value);
                        if(user)prefs.edit()
                            .putInt("s"+index,value).apply();
                    }
                    public void onStartTrackingTouch(SeekBar b){}
                    public void onStopTrackingTouch(SeekBar b){
                        log("Hassasiyet değişti: "+names[index]);
                    }
                });

            LinearLayout.LayoutParams lp=
                new LinearLayout.LayoutParams(-1,-2);
            lp.setMargins(0,3,0,5);
            content.addView(box,lp);
        }

        button("VARSAYILAN DEĞERLERİ GERİ YÜKLE",()->{
            prefs.edit().clear().apply();
            log("Hassasiyet varsayılanları yüklendi");
            showPage("AIM");
        });
    }

    private void tools(){
        heading("SYSTEM TOOLS");

        button("◉  PUBG FLASH BAŞLAT",()->launchGame());

        button("⌁  PING TESTİ",()->measurePing());

        button("✦  AI ANALİZ",()->{
            Toast.makeText(this,aiAdvice(),Toast.LENGTH_LONG).show();
            log("AI önerisi görüntülendi");
        });

        button("♧  UYGULAMA ÖNBELLEĞİNİ TEMİZLE",()->{
            cleanOwnCache();
        });

        button("◐  TEMA DEĞİŞTİR",()->{
            purpleTheme=!purpleTheme;
            prefs.edit().putBoolean("purple",purpleTheme).apply();
            log("Tema değiştirildi");
            init();
            showPage("TOOLS");
        });

        button("▤  UYGULAMA AYARLARI",()->{
            Intent i=new Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            i.setData(Uri.parse("package:"+getPackageName()));
            startActivity(i);
        });

        card("SAFETY",
            "Yalnızca izin verilen işlemler",
            "Diğer uygulamaların belleğine müdahale edilmez.");
    }

    private void cleanOwnCache(){
        try{
            File f=getCacheDir();
            File[] files=f.listFiles();
            int count=0;
            if(files!=null){
                for(File x:files){
                    if(deleteTree(x))count++;
                }
            }
            log("Kendi uygulama önbelleği temizlendi");
            Toast.makeText(this,
                "Uygulama önbelleği temizlendi",
                Toast.LENGTH_LONG).show();
        }catch(Exception e){
            Toast.makeText(this,"Temizleme başarısız",
                Toast.LENGTH_SHORT).show();
        }
    }

    private boolean deleteTree(File f){
        if(f.isDirectory()){
            File[] children=f.listFiles();
            if(children!=null)
                for(File child:children)deleteTree(child);
        }
        return f.delete();
    }

    private void history(){
        heading("ACTIVITY LOG");

        File f=new File(getFilesDir(),"activity.log");
        StringBuilder data=new StringBuilder();

        try(BufferedReader r=new BufferedReader(new FileReader(f))){
            String line;
            while((line=r.readLine())!=null)
                data.append(line).append("\n");
        }catch(Exception ignored){}

        TextView logView=text(
            data.length()==0?"Henüz kayıt yok.":data.toString(),
            11,WHITE);
        logView.setTypeface(Typeface.MONOSPACE);
        logView.setPadding(12,12,12,12);
        logView.setBackground(bg(CARD,15));
        content.addView(logView);

        button("LOG'U DOWNLOAD KLASÖRÜNE KAYDET",()->{
            log("Kullanıcı log dışa aktarma istedi");
            Toast.makeText(this,
                "Download/PROJE_KAYITLARI klasörüne kaydedildi",
                Toast.LENGTH_LONG).show();
        });

        button("LOG'U PAYLAŞ",()->{
            Intent share=new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT,data.toString());
            startActivity(Intent.createChooser(share,"Log paylaş"));
        });

        button("ANA EKRANA DÖN",()->showPage("HOME"));
    }

    @Override
    protected void onCreate(Bundle b){
        super.onCreate(b);
        prefs=getSharedPreferences("flash_ai",MODE_PRIVATE);
        purpleTheme=prefs.getBoolean("purple",false);
        init();
        log("PUBG FLASH AI BOOSTER V6 açıldı");
        showPage("HOME");
    }

    @Override
    protected void onResume(){
        super.onResume();
        handler.removeCallbacks(ticker);
        handler.post(ticker);
    }

    @Override
    protected void onPause(){
        super.onPause();
        handler.removeCallbacks(ticker);
    }

    @Override
    protected void onDestroy(){
        handler.removeCallbacks(ticker);
        super.onDestroy();
    }

    @Override
    public void onBackPressed(){
        if(!page.equals("HOME"))showPage("HOME");
        else super.onBackPressed();
    }
}
