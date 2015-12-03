package org.asdtm.goodweather;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public abstract class SingleFragmentActivity extends AppCompatActivity
{
    protected abstract Fragment createNewFragment();

    // Возвращает айди макета, заполняемого активность
    protected int getIdLayout()
    {
        return R.layout.activity_fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(getIdLayout());

        // Получаем доступ к менеджеру фрагментов
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Запрашиваем у FragmentManager фрагмент с идентификатором контейнерного представления
        // если этот фрагмент уже находится в списке, FragmentManager возвращает его
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentContainer);

        // Если фрагмент с идентификатором контейнерного представления отсутствует
        if (fragment == null) {
            // создаем новый экземпляр фрагмента
            fragment = createNewFragment();

            // Создаем новую транзакцию, которая добавляет фрагмент в список
            fragmentManager
                    .beginTransaction()
                    .add(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }
}
