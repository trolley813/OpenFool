#-------------------------------------------------
#
# Project created by QtCreator 2016-12-07T10:39:29
#
#-------------------------------------------------

QT       += core gui svg qml quick

greaterThan(QT_MAJOR_VERSION, 4): QT += widgets

TARGET = OpenFool
TEMPLATE = app
VERSION = 0.0.9

CONFIG += c++11

VPATH += src

SOURCES += main.cpp\
        mainwindow.cpp \
    carddeck.cpp \
    card.cpp \
    carditem.cpp \
    table.cpp \
    player.cpp \
    settingsdialog.cpp \
    playerbubbleitem.cpp

HEADERS  += mainwindow.h \
    carddeck.h \
    card.h \
    carditem.h \
    table.h \
    player.h \
    settingsdialog.h \
    playerbubbleitem.h

FORMS    += mainwindow.ui \
    settingsdialog.ui

RESOURCES += \
    resources.qrc

DISTFILES += \
    LICENSE \
    README.md \
    OpenFool_en.ts \
    OpenFool_ru.ts \
    CHANGELOG.md \
    android/AndroidManifest.xml \
    android/res/values/libs.xml \
    android/build.gradle \
    icons/icon_32.png \
    icons/icon_64.png \
    icons/icon_128.png \
    icons/icon_128_win.ico \
    android/res/drawable-ldpi/icon.png \
    android/res/drawable-hdpi/icon.png \
    android/res/drawable-mdpi/icon.png

TRANSLATIONS = OpenFool_en.ts OpenFool_ru.ts

ANDROID_PACKAGE_SOURCE_DIR = $$PWD/android

RC_ICONS = icons/icon_128_win.ico
