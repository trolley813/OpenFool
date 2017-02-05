#-------------------------------------------------
#
# Project created by QtCreator 2016-12-07T10:39:29
#
#-------------------------------------------------

QT       += core gui svg

greaterThan(QT_MAJOR_VERSION, 4): QT += widgets

TARGET = OpenFool
TEMPLATE = app
VERSION = 0.0.3

CONFIG += c++11

SOURCES += main.cpp\
        mainwindow.cpp \
    carddeck.cpp \
    card.cpp \
    carditem.cpp \
    table.cpp \
    player.cpp \
    settingsdialog.cpp

HEADERS  += mainwindow.h \
    carddeck.h \
    card.h \
    carditem.h \
    table.h \
    player.h \
    settingsdialog.h

FORMS    += mainwindow.ui \
    settingsdialog.ui

RESOURCES += \
    resources.qrc

DISTFILES += \
    LICENSE \
    README.md \
    OpenFool_en.ts \
    OpenFool_ru.ts \
    CHANGELOG.md

TRANSLATIONS = OpenFool_en.ts OpenFool_ru.ts
