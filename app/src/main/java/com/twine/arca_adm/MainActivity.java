package com.twine.arca_adm;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.twine.arca_adm.general.Utilidades;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity
public class MainActivity extends AppCompatActivity implements
        DashboardFragment.OnFragmentInteractionListener,
        DescuentosFragment.OnFragmentInteractionListener,
        PerfilFragment.OnFragmentInteractionListener{
    @ViewById(R.id.content)
    FrameLayout content;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentManager fm = MainActivity.this.getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            switch (item.getItemId()) {

                case R.id.navigation_dashboard:
                    ft.replace(R.id.content, new DashboardFragment_(), "DashboardTaeFragmentTag");
                    ft.addToBackStack("DashboardTaeFragmentTag");
                    ft.commit();
                    fm.executePendingTransactions();
                    return true;
                case R.id.navigation_descuento:
                    ft.replace(R.id.content, new DescuentosFragment_(), "DescuentosFragmentTag");
                    ft.addToBackStack("DescuentosFragmentTag");
                    ft.commit();
                    fm.executePendingTransactions();
                    return true;
                case R.id.navigation_perfil:
                    ft.replace(R.id.content, new PerfilFragment_(), "PerfilFragmentTag");
                    ft.addToBackStack("PerfilFragmentTag");
                    ft.commit();
                    fm.executePendingTransactions();
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(Utilidades.atuenticado_o_redirect(this)){
            Utilidades.checkPermision(MainActivity.this);
            BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

            FragmentManager fm = MainActivity.this.getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.content, new DashboardFragment_(), "DashboardTaeFragmentTag");
            ft.addToBackStack("DashboardTaeFragmentTag");
            ft.commit();
            fm.executePendingTransactions();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                android.app.AlertDialog.Builder saveDialog = new android.app.AlertDialog.Builder(MainActivity.this);
                saveDialog.setTitle("Permisos de la aplicacion");
                saveDialog.setMessage("La aplicacion aun necesita que usted otorgue algunos permisos\n\n" +
                        "Si desea otorgarlos ahora, haga clic [Si] y en el siguiente menu contextual clic en [Permitir]. \n\n" +
                        "Si por el contrario hace clic [No] este modulo procederá a cerrarse");
                saveDialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        Utilidades.checkPermision(MainActivity.this);
                    }
                });
                saveDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                saveDialog.show();
                return;
            }
        }
    }
    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
