#ifndef SETTINGSDIALOG_H
#define SETTINGSDIALOG_H

#include <QAbstractButton>
#include <QDialog>
#include <QSettings>

namespace Ui
{
class SettingsDialog;
}

class SettingsDialog : public QDialog
{
    Q_OBJECT

public:
    explicit SettingsDialog(QSettings *settings, QWidget *parent = 0);
    ~SettingsDialog();
    void saveSettings();
public slots:
    void accept() override;
    void apply();
    void onClick(QAbstractButton *button);

private slots:
    void on_pushButtonSelectColor_clicked();

private:
    Ui::SettingsDialog *ui;
    QSettings *settings;
    QColor backgroundColor;
};

#endif // SETTINGSDIALOG_H
