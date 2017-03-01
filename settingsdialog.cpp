#include "settingsdialog.h"
#include "ui_settingsdialog.h"
#include <QColorDialog>

static QMap<QString, QString> LANGUAGES = {
    {"en", QObject::tr("English")}, {"ru", QObject::tr("Russian")},
};

const QColor DEFAULT_BACKGROUND(86, 156, 30, 230);

SettingsDialog::SettingsDialog(QSettings *settings, QWidget *parent)
    : QDialog(parent), ui(new Ui::SettingsDialog), settings(settings)
{
    ui->setupUi(this);

    ui->lineEditPlayer1Name->setText(
        settings->value("players/name1", "South").toString());
    ui->lineEditPlayer2Name->setText(
        settings->value("players/name2", "West").toString());
    ui->lineEditPlayer3Name->setText(
        settings->value("players/name3", "North").toString());
    ui->lineEditPlayer4Name->setText(
        settings->value("players/name4", "East").toString());

    ui->checkBoxGL->setChecked(
        settings->value("rendering/opengl", false).toBool());

    ui->radioButtonRus->setChecked(settings->value("cards/deck", "rus")
                                   == "rus");
    ui->radioButtonInt->setChecked(settings->value("cards/deck", "rus")
                                   == "int");
    ui->radioButtonFra->setChecked(settings->value("cards/deck", "rus")
                                   == "fra");

    ui->radioButtonUnsorted->setChecked(settings->value("rendering/sorting", 0)
                                        == 0);
    ui->radioButtonAscending->setChecked(settings->value("rendering/sorting", 0)
                                        == 1);
    ui->radioButtonDescending->setChecked(settings->value("rendering/sorting", 0)
                                        == 2);

    for (auto language : LANGUAGES)
        ui->comboBoxLanguage->addItem(language);
    ui->comboBoxLanguage->setCurrentIndex(LANGUAGES.keys().indexOf(
        settings->value("general/language", "").toString()));
    backgroundColor = settings->value("rendering/background",
                                      DEFAULT_BACKGROUND).value<QColor>();
}

SettingsDialog::~SettingsDialog() { delete ui; }

void SettingsDialog::saveSettings()
{
    settings->setValue("players/name1", ui->lineEditPlayer1Name->text());
    settings->setValue("players/name2", ui->lineEditPlayer2Name->text());
    settings->setValue("players/name3", ui->lineEditPlayer3Name->text());
    settings->setValue("players/name4", ui->lineEditPlayer4Name->text());

    settings->setValue("rendering/opengl", ui->checkBoxGL->isChecked());

    if (ui->radioButtonRus->isChecked())
        settings->setValue("cards/deck", "rus");
    if (ui->radioButtonInt->isChecked())
        settings->setValue("cards/deck", "int");
    if (ui->radioButtonFra->isChecked())
        settings->setValue("cards/deck", "fra");

    if(ui->radioButtonUnsorted->isChecked())
        settings->setValue("rendering/sorting", 0);
    if(ui->radioButtonAscending->isChecked())
        settings->setValue("rendering/sorting", 1);
    if(ui->radioButtonDescending->isChecked())
        settings->setValue("rendering/sorting", 2);

    settings->setValue("general/language",
                       LANGUAGES.key(ui->comboBoxLanguage->currentText()));
    settings->setValue("rendering/background", backgroundColor);
}

void SettingsDialog::accept()
{
    saveSettings();
    QDialog::accept();
}

void SettingsDialog::apply() { saveSettings(); }

void SettingsDialog::onClick(QAbstractButton *button)
{
    if (ui->buttonBox->standardButton(button) == QDialogButtonBox::Apply)
        apply();
}

void SettingsDialog::on_pushButtonSelectColor_clicked()
{
    backgroundColor = QColorDialog::getColor(backgroundColor, this,
                                             tr("Select background color"));
}
