package com.noooobas.blc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.sqlite.db.SimpleSQLiteQuery;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public Button  search_btn, import_btn, export_btn, do_action_btn,cancel_btn;
    public TextView tv_percent, tv_total_sum, card_num_field, add_sum_field;
    Dialog_Fragment dlg;
    private static final int PERMISSIONS_REQUEST_CODE = 101;
    public static final String FILE_NAME = "Exported.CSV";
    public static final String BACKUP_FILE_NAME = "Exported_backup.CSV";
    public static final String FOLDER_NAME = "Exported data";
    public static final String TABLE_NAME = "CardDB";

    File exportDir = new File
            (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FOLDER_NAME);
    String FILE_PATH = exportDir.getPath();

    String[] spinner_options = {"Добавить:","Вычесть:", "Изменить на: "};
    String[] actions = {"Сумма добавлена", "Сумма вычтена", "Сумма изменена"};

    int selected_action = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dlg = new Dialog_Fragment();

        search_btn = findViewById(R.id.search_btn);
        import_btn = findViewById(R.id.import_btn);
        export_btn = findViewById(R.id.export_btn);
        do_action_btn = findViewById(R.id.do_action_btn);
        cancel_btn = findViewById(R.id.cancel_btn);

        card_num_field = findViewById(R.id.card_num_field);
        add_sum_field = findViewById(R.id.add_sum_field);

        search_btn.setOnClickListener(this);
        import_btn.setOnClickListener(this);
        export_btn.setOnClickListener(this);
        do_action_btn.setOnClickListener(this);
        cancel_btn.setOnClickListener(this);

        tv_percent = findViewById(R.id.tv_percent);
        tv_total_sum = findViewById(R.id.tv_total_sum);



        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            String PERM_CHECK = "Permission Check";
            Log.i(PERM_CHECK, "Permission to write denied");
            makeRequest();
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, spinner_options);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        Spinner spinner = findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        spinner.setPrompt("Выберите действие");
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selected_action = i;
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

    }

    @Override
    protected void onDestroy() {
        AppDatabase.destroyInstance();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        AppDatabase db = AppDatabase.getAppDatabase(this);
        CardDao cardDao = db.cardDao();
        switch (view.getId())
        {
            case R.id.search_btn:
                if (card_num_field.getText().toString().isEmpty())
                    Toast.makeText(this, "Сначала введи номер карты.",Toast.LENGTH_SHORT).show();
                else check_existence(cardDao);
                break;
            case R.id.do_action_btn:
                if (add_sum_field.getText().toString().isEmpty())
                    Toast.makeText(this, "Сначала введи значение.",Toast.LENGTH_SHORT).show();
                else do_action(cardDao);
                break;
            case R.id.import_btn:
                try {
                    import_data(cardDao);
                } catch (FileNotFoundException e) {
                    Toast.makeText(this,"Файл не найден. Поместите файл в "+FILE_PATH+ " и повторите операцию.",Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.export_btn:
                export_data(FILE_NAME);
                break;
            case R.id.cancel_btn:
                back_to_start();
                break;
        }
    }
    public void export_data(String filename){
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        File file = new File(exportDir, filename);
        try {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            Cursor curCSV = AppDatabase.getAppDatabase(this).query("SELECT * FROM "+ TABLE_NAME, null);
            csvWrite.writeNext(curCSV.getColumnNames());
            while (curCSV.moveToNext()) {
                //Which column you want to export
                String arrStr[] = new String[curCSV.getColumnCount()];
                for (int i = 0; i < curCSV.getColumnCount(); i++)
                    arrStr[i] = curCSV.getString(i);
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
            Toast.makeText(this, "Файл сохранён в "+FILE_PATH, Toast.LENGTH_LONG).show();
        } catch (Exception sqlEx) {
            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
        }
    }

    public void import_data (CardDao cardDao) throws IOException {
        export_data(BACKUP_FILE_NAME);
        cardDao.nukeTable();
        CSVReader csvReader = new CSVReader(new FileReader(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + FOLDER_NAME + "/" + FILE_NAME));
        String[] nextLine;
        int count = 0;
        StringBuilder columns = new StringBuilder();
        StringBuilder value = new StringBuilder();
        int cards_imported = 0;

        while ((nextLine = csvReader.readNext()) != null) {
            // nextLine[] is an array of values from the line
            for (int i = 0; i < nextLine.length; i++) {
                if (count == 0) {
                    if (i == nextLine.length - 1)
                        columns.append(nextLine[i]);
                    else
                        columns.append(nextLine[i]).append(",");
                } else {
                    if (i == nextLine.length - 1)
                        value.append("'").append(nextLine[i]).append("'");
                    else
                        value.append("'").append(nextLine[i]).append("',");
                }

            }
            String DATA_IMPORT = "Imported data";
            Log.d(DATA_IMPORT, columns + "-------" + value);
            SimpleSQLiteQuery query =
                    new SimpleSQLiteQuery("Insert INTO " + TABLE_NAME + " (" + columns + ") " + "VALUES(" + value + ")", new Object[]{});
            if(count!=0) {
                cardDao.insertDataRawFormat(query);
                value.setLength(0);
                cards_imported++;
            }
            count = 1;
        }
             Toast.makeText(this, "Бэкап создан. Импортировано карт: "+cards_imported, Toast.LENGTH_SHORT).show();
    }

   public void do_action(CardDao cardDao){
       int cardNum = Integer.parseInt(card_num_field.getText().toString());
       CardDB oldCard = cardDao.getById(cardNum);
       long sum = Long.parseLong(add_sum_field.getText().toString());
       switch (selected_action){
           case 0:
               oldCard.purSum += sum;
               break;
           case 1:
               if (oldCard.purSum < sum){
                   Toast.makeText(this, "Вычитаемая сумма не может быть меньше текущего значения.", Toast.LENGTH_SHORT).show();
                   return;
               }
                else oldCard.purSum-= sum;
               break;
           case 2:
                oldCard.purSum = sum;
       }
       cardDao.update(oldCard);
       Toast.makeText(this, actions[selected_action], Toast.LENGTH_SHORT).show();
       back_to_start();
   }

   public int calc_percent(long pur_sum){
    if (pur_sum <= 9999)
        return 3;
    else if(pur_sum <= 29999)
        return 5;
    else if(pur_sum <= 49999)
        return 7;
    else return 10;
   }

   public void check_existence(CardDao cardDao){
        int cardNum = Integer.parseInt(card_num_field.getText().toString());
        if(!cardDao.checkExist(cardNum)) {
           dlg.show(getSupportFragmentManager(),"dlg");
       }
       else {
           CardDB card = cardDao.getById(cardNum);
           set_fields(card.purSum);
       }
   }

   protected void makeRequest(){
       ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSIONS_REQUEST_CODE);
       ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSIONS_REQUEST_CODE);
   }

   public void set_fields(long purSum){
        String totalSum = getString(R.string.total_sum,purSum);
        String percent = getString(R.string.current_percent,calc_percent(purSum));
        tv_total_sum.setText(totalSum);
       tv_percent.setText(percent);
       card_num_field.setEnabled(false);
       search_btn.setVisibility(View.GONE);
       export_btn.setVisibility(View.INVISIBLE);
       import_btn.setVisibility(View.INVISIBLE);
       findViewById(R.id.search_layout).setVisibility(View.VISIBLE);
   }

   public void back_to_start(){
       findViewById(R.id.search_layout).setVisibility(View.INVISIBLE);
       card_num_field.setText("");
       add_sum_field.setText("");
       card_num_field.setEnabled(true);
       search_btn.setVisibility(View.VISIBLE);
       export_btn.setVisibility(View.VISIBLE);
       import_btn.setVisibility(View.VISIBLE);
   }

   public void add_positive() {
       int cardNum = Integer.parseInt(card_num_field.getText().toString());
       AppDatabase.getAppDatabase(this).cardDao().insert_new(cardNum);
       Toast.makeText(this, "Карта добавлена",Toast.LENGTH_SHORT).show();
       set_fields(0);
   }

}

