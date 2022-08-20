Name:           frdl
Version:        0.29.0
Release:        %(date +%Y%m%d%H)%{?dist}
Summary:        CIMA FRDL Flight Recorder Downloader

Group:          Applications/Internet
License:        GPLv3
URL:            https://github.com/jmlich/frdl
Source0:        https://github.com/jmlich/frdl/archive/master.tar.gz#/%{name}-%{version}.tar.gz

BuildRequires:  desktop-file-utils
BuildRequires:  ant
BuildRequires:  java-devel

Requires: java >= 1:1.6.0

BuildArch: noarch



%description
FRDL is designed for Microlight and Paramotor competition organizers, and for team leaders or pilots
to all quickly reliably and painlessly extract track data relavent to competition tasks from a variety
of different GPS flight loggers, and then convert it into the FAI CIMA specified format for detailed
examination in a flight analysis program.

FRDL backs up all log files it finds on a logger and instantly displays a simple outline and altitude
profile of the track it has written to the output .igc file. This means the downloading can be done
the moment the pilot lands from a championship task, and in the mutual knowledge of what has been saved,
the logger can confidently be immediately returned to the pilot.

FRDL maintains all the information it needs about the championship, championship tasks, and the loggers
in use in a championship file (*.frdc). Once a championship file is open, FRDL is ready for use.

%global debug_package %{nil}

%prep
%setup -q -n %{name}-master


%build
ant

%install
mkdir -p %{buildroot}%{_javadir}
mkdir -p %{buildroot}%{_javadir}/lib
mkdir -p %{buildroot}%{_datadir}/icons/hicolor/32x32/apps/
install -p -m 644 dist/FRDL.jar %{buildroot}%{_javadir}/FRDL.jar

for lib in $(find dist/lib -type f); do 
    libbn="$(basename "$lib")"
    install -p -m 644 "$lib" "%{buildroot}%{_javadir}/lib/$libbn"
    echo "%{_javadir}/lib/$libbn" >> libs.list
done

desktop-file-install --dir=${RPM_BUILD_ROOT}%{_datadir}/applications %{name}.desktop
install -p -m 644 jet32.png ${RPM_BUILD_ROOT}%{_datadir}/icons/hicolor/32x32/apps/FRDL.png

# 1    main class
# 2    flags
# 3    options
# 4    jars (separated by ':')
# 5    the name of script you wish to create
# 6    whether to prefer a jre over a sdk when finding a jvm

%jpackage_script FRDL.App "" "" FRDL FRDL true

%files -f libs.list
%{_bindir}/FRDL
%{_javadir}/FRDL.jar
%{_datadir}/applications/%{name}.desktop
%{_datadir}/icons/hicolor/32x32/apps/FRDL.png


%changelog
* Fri Jun 15 2018 Jozef Mlich <imlich@fit.vutbr.cz> - 0.29.0-1
- initial packaging

