package org.asdtm.goodweather;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

public abstract class SingleFragmentActivity extends Activity
{
    protected abstract Fragment createNewFragment();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        // Получаем доступ к менеджеру фрагментов
        FragmentManager fragmentManager = getFragmentManager();

        // Запрашиваем у FragmentManager фрагмент с идентификатором контейнерного представления
        // если этот фрагмент уже находится в списке, FragmentManager возвращает его
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentContainer);

        // Если фрагмент с идентификатором контейнерного представления отсутствует
        if (fragment == null) {
            // создаем новый экземпляр фрагмента
            fragment = createNewFragment();

            // Создаем новую транзакцию, которая добавляет фрагмент в список
            fragmentManager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }


    }
}
