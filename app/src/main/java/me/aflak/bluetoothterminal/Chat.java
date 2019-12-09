package me.aflak.bluetoothterminal;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.LightingColorFilter;
import android.graphics.PorterDuff;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.TimeZone;

import me.aflak.bluetooth.Bluetooth;

public class Chat extends AppCompatActivity implements Bluetooth.CommunicationCallback, DatePickerDialog.OnDateSetListener {
    private String name;
    private Bluetooth b;
    private EditText message;
    private Button send;
    private TextView text;
    private ScrollView scrollView;
    private Button lightb;
    private Button timeButton;
    private Button reset;
    private EditText pesoInput;
    private EditText dosisInput;
    private EditText gramosInput;
    private boolean registered=false;
    private boolean lightBgColor = false;
    private ImageView connected;

    public final Calendar c = Calendar.getInstance();

    // final int hora = c.get(Calendar.HOUR_OF_DAY);
    // final int minuto = c.get(Calendar.MINUTE);

    int hora = 0;
    int minuto = 0;

    public static final void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = (TextView)findViewById(R.id.text);
        message = (EditText)findViewById(R.id.message);
        send = (Button)findViewById(R.id.send);
        lightb = (Button)findViewById(R.id.light_button);
        reset = (Button)findViewById(R.id.reset_button);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        timeButton = (Button) findViewById(R.id.timePicker);
        pesoInput = (EditText) findViewById(R.id.pesoInput);
        dosisInput = (EditText) findViewById(R.id.dosis_input);
        gramosInput = (EditText) findViewById(R.id.gramos_input);
        connected = (ImageView) findViewById(R.id.connectionSatus);



        dosisInput.setEnabled(false);
        gramosInput.setEnabled(false);
        pesoInput.setEnabled(false);
        lightb.setEnabled(false);
        timeButton.setEnabled(false);
        send.setEnabled(false);
        reset.setEnabled(false);
        connected.setImageResource(R.drawable.disconnect);

        lightb.setBackground(getResources().getDrawable(R.drawable.rounded_gray));
        text.setMovementMethod(new ScrollingMovementMethod());


        b = new Bluetooth(this);
        b.enableBluetooth();

        b.setCommunicationCallback(this);

        int pos = getIntent().getExtras().getInt("pos");
        name = b.getPairedDevices().get(pos).getName();
        //b.getPairedDevices().get(2)

        Display("Conectando...");
        /*for (BluetoothDevice d : paired){
            if(d.getName().contains("Autosulivan's"))
            names.add(d.getName());
        }
        */

        b.connectToDevice(b.getPairedDevices().get(pos));

        gramosInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String gramos;
                //Toast.makeText(getApplicationContext(), "Texto de muestra: " + actionId, Toast.LENGTH_SHORT).show();
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    gramos = "gramos " +  gramosInput.getText().toString();
                    //b.send("gramos " +  dosisInput.getText().toString());

                    Toast.makeText(getApplicationContext(), gramos, Toast.LENGTH_LONG).show();

                    //Toast.makeText(getApplicationContext(), "Gramos por racion establecido en: " + Integer.parseInt(pesoInput.getText().toString()) / 1000, Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });



        dosisInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    b.send("dosis " + dosisInput.getText().toString());
                    Toast.makeText(getApplicationContext(), "Raciones establecido en: " + Integer.parseInt(pesoInput.getText().toString()) / 1000, Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });

        pesoInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String peso;
                //Toast.makeText(getApplicationContext(), "Texto de muestra: " + actionId, Toast.LENGTH_SHORT).show();
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //peso = String.format("%05d" , Integer.parseInt(pesoInput.getText().toString()));
                    b.send("peso " + String.format("%05d" , Integer.parseInt(pesoInput.getText().toString())));
                    Toast.makeText(getApplicationContext(), "Peso establecido en: " + Integer.parseInt(pesoInput.getText().toString()) / 1000, Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = message.getText().toString();
                message.setText("");
                b.send(msg);
                Display("You: "+msg);
            }
        });

        lightb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (lightBgColor){
                    lightb.setBackground(getResources().getDrawable(R.drawable.rounded_gray));
                    //lightb.getBackground().setColorFilter(new LightingColorFilter(0xFFDCDCDC, 0xFFFFFFFF));
                    //lightb.setBackgroundColor(0xFFDCDCDC);
                    //lightBgColor = false;
                }
                else {
                    lightb.setBackground(getResources().getDrawable(R.drawable.rounded));
                    //lightb.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFFDCDCDC));
                    //lightb.setBackgroundColor(0xFFFFFFFF);
                    //lightBgColor = true;
                }
                lightBgColor = !lightBgColor;
                String msg = "luz";
                //message.setText("");
                b.send(msg);
                Display("You: "+msg);
            }
        });

        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Este es mi listener para el timebutton", Toast.LENGTH_SHORT);
                try {
                    obtenerHora();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "Este es mi listener para el timebutton", Toast.LENGTH_SHORT);

                //fijarHora();
            }
        });


        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        registered=true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(registered) {
            unregisterReceiver(mReceiver);
            registered=false;
        }
    }

    private void obtenerHora(){
        TimePickerDialog recogerHora = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                String horaFormateada =  (hourOfDay < 9)? String.valueOf("0" + hourOfDay) : String.valueOf(hourOfDay);
                String minutoFormateado = (minute < 9)? String.valueOf("0" + minute):String.valueOf(minute);

                String AM_PM;
                if(hourOfDay < 12) {
                    AM_PM = "a.m.";
                } else {
                    AM_PM = "p.m.";
                }

                //Toast.makeText(getApplicationContext(), "Fecha fijada", Toast.LENGTH_SHORT);
                Toast.makeText(getApplicationContext(),horaFormateada + " : " + minutoFormateado + " " + AM_PM,  Toast.LENGTH_SHORT);
                fijarHora(hourOfDay, minute);
                //etHora.setText(horaFormateada + DOS_PUNTOS + minutoFormateado + " " + AM_PM);
            }

        }, hora, minuto, true);

        recogerHora.show();

        //fijarHora();
    }

    private void fijarHora(int hourOfDay, int minute){

        String horaFormateada =  (hourOfDay < 9)? String.valueOf("0" + hourOfDay) : String.valueOf(hourOfDay);
        String minutoFormateado = (minute < 9)? String.valueOf("0" + minute):String.valueOf(minute);

        String msg = "comida " + horaFormateada + ":" + minutoFormateado;
        //message.setText("");
        b.send(msg);
        Display("You: "+msg);
        Display(TimeZone.getDefault().getDisplayName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {

            case R.id.command_option:

                /*
                LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                PopupWindow pw = new PopupWindow(inflater.inflate(R.layout.popup, null, false),100,100, true);

                pw.showAtLocation( findViewById(R.id.message), Gravity.CENTER, 0, 0);
*/

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Este es el manual del comedero.\n----------------------------------------\nResponde a los siguientes comandos:\nUltimo\nComida\nHora\nDosis\nGramos\nPeso\nReset\nLuz\nTest\nMan\n\nPara mostrar el uso de los comandos\nescribir: Man [comando]. P.Ej.: Man Luz\n**No distingue mayusculas.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

                return true;

            case R.id.close:
                b.removeCommunicationCallback();
                b.disconnect();
                Intent intent = new Intent(this, Select.class);
                startActivity(intent);
                finish();
                return true;

            case R.id.rate:
                Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void Display(final String s){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.append(s + "\n");
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void ponerEnHora(){
        int hora = c.get(Calendar.HOUR_OF_DAY);
        int minuto = c.get(Calendar.MINUTE);
        int segundo = c.get(Calendar.SECOND);
        int dia = c.get(Calendar.DAY_OF_MONTH);
        int mes = c.get(Calendar.MONTH);
        int año = c.get(Calendar.YEAR);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        String texto = String.format("hora %02d:%02d:%02d %02d-%02d-%04d %01d" , hora, minuto, segundo, dia, mes, año, dayOfWeek);
        b.send(texto);
        //Toast.makeText(getApplicationContext(), texto, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onConnect(BluetoothDevice device) {
        Display("Conectado a: "+ device.getName()+" - "+ device.getAddress());

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                dosisInput.setEnabled(true);
                gramosInput.setEnabled(true);
                pesoInput.setEnabled(true);
                lightb.setEnabled(true);
                timeButton.setEnabled(true);
                send.setEnabled(true);
                connected.setImageResource(R.drawable.connect);
                send.setEnabled(true);
                reset.setEnabled(true);
                ponerEnHora();
            }
        });
    }

    public void fijarPeso(){
        Display("Este es mi texto del peso");
    }

    @Override
    public void onDisconnect(BluetoothDevice device, String message) {
        Display("Desconectado!");
        Display("Reconectando");
        b.connectToDevice(device);

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                dosisInput.setEnabled(false);
                gramosInput.setEnabled(false);
                pesoInput.setEnabled(false);
                lightb.setEnabled(false);
                timeButton.setEnabled(false);
                send.setEnabled(false);
                reset.setEnabled(false);
                connected.setImageResource(R.drawable.disconnect);
            }
        });
    }

    @Override
    public void onMessage(String message) {

        if(message.contains("Se ha actualizado")){
            String texto = String.format(" %02d:%02d:%02d %02d-%02d-%04d %01d" , c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH), c.get(Calendar.YEAR), c.get(Calendar.DAY_OF_WEEK));
            message = message + texto;
        }

        if(message.length() > 2) {
            Display("* " + message);
        }

    }

    @Override
    public void onError(String message) {
        Display("Error: "+message);
    }

    @Override
    public void onConnectError(final BluetoothDevice device, String message) {
        Display("Error: "+message);
        Display("Reintentando en 3 sec.");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        b.connectToDevice(device);
                    }
                }, 2000);
            }
        });
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                Intent intent1 = new Intent(Chat.this, Select.class);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        if(registered) {
                            unregisterReceiver(mReceiver);
                            registered=false;
                        }
                        startActivity(intent1);
                        finish();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        if(registered) {
                            unregisterReceiver(mReceiver);
                            registered=false;
                        }
                        startActivity(intent1);
                        finish();
                        break;
                }
            }
        }
    };

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        Toast.makeText(getApplicationContext(), "Fecha fijada", Toast.LENGTH_SHORT);
    }
}
