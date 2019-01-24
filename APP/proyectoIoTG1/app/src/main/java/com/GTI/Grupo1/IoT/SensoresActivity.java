package com.GTI.Grupo1.IoT;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class SensoresActivity extends AppCompatActivity{

    private RecyclerView recyclerView;
    public SensoresAdaptador adaptador;
    private RecyclerView.LayoutManager layoutManager;

    public static SensoresInterface sensores = new SensoresVector();

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensores_layout);

        //recyclerView = findViewById(R.id.recycler_view);
        adaptador = new SensoresAdaptador(this, sensores);
        recyclerView.setAdapter(adaptador);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

    }
}
