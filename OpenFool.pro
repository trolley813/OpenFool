#-------------------------------------------------
#
# Project created by QtCreator 2016-12-07T10:39:29
#
#-------------------------------------------------

QT       += core gui svg

greaterThan(QT_MAJOR_VERSION, 4): QT += widgets

TARGET = OpenFool
TEMPLATE = app
VERSION = 0.0.6

CONFIG += c++11

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
    android/build.gradle

TRANSLATIONS = OpenFool_en.ts OpenFool_ru.ts

ANDROID_PACKAGE_SOURCE_DIR = $$PWD/android
