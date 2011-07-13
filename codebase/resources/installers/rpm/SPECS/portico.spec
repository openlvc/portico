#                                                              #
# Defines: Many of these will be passed in on the command line #
#                                                              #

# defines that should be provided on command line:
#  -> version:   x.x
#  -> jrehome:  /some/given/path

# other defines
%define name       portico
%define release    1
%define fullname   %{name}-%{version}
%define installdir /opt/portico/%{fullname}

#===== Spec Header ===========================================================
Summary:      Open Source RTI implementation
Name:         %{name}
Version:      %{version}
Release:      %{release}
Source0:      %{name}-%{version}-linux.tar.gz
Vendor:       The Portico Project
URL:          http://www.porticoproject.org
License:      CDDL
Group:        Applications/Communications
BuildArch:    noarch
BuildRoot:    %{_builddir}/%{name}-root

#==== Description ============================================================
%description
Portico is an open source, extensible, portable RTI implementation for the HLA.

#==== Preparation ============================================================
%prep
%setup

echo '>> done prep'

#==== Build ==================================================================
%build

#==== Install ================================================================
%install

# copy the install files to the fake build dir location
mkdir -p $RPM_BUILD_ROOT/%{installdir}
cp -Rf * $RPM_BUILD_ROOT/%{installdir}

# 1. copy across the jre files into the building root
#mkdir $RPM_BUILD_ROOT/%{installdir}/jre
#cp -Rf %{jrehome}/* $RPM_BUILD_ROOT/%{installdir}/jre/

# update the scripts so that the appropriate information is supplied
# 2. set the RTI_HOME (just add the RTI_HOME declaration to the top of the file)
#sed -i s:JAVA=java:JAVA=java\\nRTI_HOME=%{installdir}: $RPM_BUILD_ROOT/%{installdir}/bin/portico
#sed -i s:JAVA=java:JAVA=java\\nRTI_HOME=%{installdir}: $RPM_BUILD_ROOT/%{installdir}/bin/portico-embedded
#sed -i s:JAVA=java:JAVA=java\\nRTI_HOME=%{installdir}: $RPM_BUILD_ROOT/%{installdir}/bin/rticonsole

# 3. set the JAVA_HOME to the local JRE if the current JAVA_HOME env var isn't set
#sed -i 's:echo WARNING Your JAVA_HOME environment variable is not set!:echo WARNING Your JAVA_HOME environment variable is not set. Using embedded version\n\tJAVA=%{installdir}/jre/bin/java:' $RPM_BUILD_ROOT/%{installdir}/bin/portico
#sed -i 's:echo WARNING Your JAVA_HOME environment variable is not set!:echo WARNING Your JAVA_HOME environment variable is not set. Using embedded version\n\tJAVA=%{installdir}/jre/bin/java:' $RPM_BUILD_ROOT/%{installdir}/bin/portico-embedded
#sed -i 's:echo WARNING Your JAVA_HOME environment variable is not set!:echo WARNING Your JAVA_HOME environment variable is not set. Using embedded version\n\tJAVA=%{installdir}/jre/bin/java:' $RPM_BUILD_ROOT/%{installdir}/bin/rticonsole

echo '>> done install'

#==== Post-Install ===========================================================
%post

# fix permissions, create the log directory and fix its permissions
chmod 0777 %{installdir}
mkdir %{installdir}/log
chmod 0777 %{installdir}/log

#==== Clean ==================================================================
%clean
rm -Rf $RPM_BUILD_ROOT/%{installdir}
rm -Rf %{_builddir}/*
#mkdir %{_builddir}
echo '>> done clean'

#==== Post-Uninstall =========================================================
%postun
# RPM doesn't seem to clean up everything, so this should make sure
#rm -Rf $RPM_BUILD_ROOT/%{installdir}

#==== Files ==================================================================
%files
%defattr(-,root,root)
%{installdir}
#%{installdir}/LICENSE.portico
#%{installdir}/bin
#%{installdir}/etc
#%{installdir}/lib
#%{installdir}/plugin
#%{installdir}/README
#%{installdir}/SOURCE_CODE
#%{installdir}/documentation

#==== Changelog ==============================================================
%changelog
* Tue May 8 2007 T. Pokorny <tim@porticoproject.org>
-First proper draft of the spec file

