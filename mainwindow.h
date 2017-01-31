#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>
#include "table.h"

namespace Ui
{
class MainWindow;
}

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    explicit MainWindow(QWidget *parent = 0);
    ~MainWindow();
    void resizeEvent(QResizeEvent *event) override;

private slots:
    void on_actionNew_Game_triggered();

    void on_actionStats_triggered();

    void on_actionSettings_triggered();

    void on_actionQuit_triggered();

    void on_actionHelp_triggered();

    void on_actionAbout_triggered();

private:
    Ui::MainWindow *ui;
    Table *table;
};

#endif // MAINWINDOW_H
