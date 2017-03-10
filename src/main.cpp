#include "mainwindow.h"
#include <QApplication>
#include <QTranslator>
#include <QLibraryInfo>
#include <QGuiApplication>
#include <QQmlApplicationEngine>

int main(int argc, char *argv[])
{
    // QApplication a(argc, argv);
    QGuiApplication a(argc, argv);

    a.setQuitOnLastWindowClosed(true);

    QSettings s(QSettings::IniFormat, QSettings::UserScope, "hyst329",
                "OpenFool");

    QString language = s.value("general/language",
                               QLocale::system().name().left(2)).toString();
    s.setValue("general/language", language);

    QTranslator qtTranslator;
    qtTranslator.load("qt_" + language,
                      QLibraryInfo::location(QLibraryInfo::TranslationsPath));
    a.installTranslator(&qtTranslator);

    QTranslator openFoolTranslator;
    openFoolTranslator.load(
        "OpenFool_" + language,
        QLibraryInfo::location(QLibraryInfo::TranslationsPath));
    a.installTranslator(&openFoolTranslator);

    //    MainWindow w;
    //    w.show();

    QQmlApplicationEngine engine;
    engine.load(QUrl(QStringLiteral("qrc:/qml/Main.qml")));

    return a.exec();
}
