package com.example.library.forms;

import javax.swing.*;

public interface CalendarPanelListener {
    void onCreateClass();
    void onManageClass();
    void onManageEvents();
    void onLogout();
    void onHome();
    void onChangeMonth(int delta);
    void updateCalendar(JPanel calendarPanel, JLabel monthLabel);
    void onNext7DaysButtonClicked();//Christian// added button here
}
