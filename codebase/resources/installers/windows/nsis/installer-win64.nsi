;NSIS Modern User Interface
;Portico Installation Script
;Written by Tim Pokorny

!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_BITMAP "header.bmp"
!define MUI_HEADERIMAGE_BITMAP_NOSTRETCH
!define MUI_WELCOMEFINISHPAGE_BITMAP "welcome.bmp"
;!define MUI_WELCOMEFINISHPAGE_BITMAP_NOSTRETCH

;--------------------------------
;Include Modern UI

  !include "MUI.nsh"

;--------------------------------
;General

  ;Name and file
  Name "Portico v${VERSION} (64-bit)"
  OutFile "${OUTDIR}\portico-${VERSION}-win64.exe"

  ;Default installation folder
  InstallDir "$PROGRAMFILES64\Portico\portico-${VERSION}"
  
  ;Other Misc properties
  BrandingText /TRIMRIGHT "Portico v${VERSION} (64-bit)"

  VIProductVersion "${VERSION}.${BUILD_NUMBER}"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "ProductName" "Portico"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "Comments" "The Portico Open Source RTI"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "CompanyName" "The OpenLVC Group"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "LegalCopyright" "The OpenLVC Group"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "FileDescription" "Portico Installer"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "FileVersion" "${VERSION}.${BUILD_NUMBER}"

;--------------------------------
;Interface Settings

  ;the following flag means that the installer will show a warning
  ;before it exists if the user chooses to abort the install
  !define MUI_ABORTWARNING

;--------------------------------
;Pages
  
  !insertmacro MUI_PAGE_WELCOME
  !insertmacro MUI_PAGE_LICENSE "${LICENSE}"
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES
  
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  
;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "English"

;--------------------------------
;Installer Sections

Section "Portico" SecPORTICO

  SetOutPath "$INSTDIR"
  
  ;ADD YOUR OWN FILES HERE...
  File /r "${SANDBOX}\*.*"      ;copy the sandbox in, without bin and lib
  RMDir /r "$INSTDIR\lib"       ;remove the lib and bin dirs (we'll put them back in a second)
  RMDir /r "$INSTDIR\bin"
  File /r "${SANDBOX}\*64*.*"   ;copy back only the 64-bit libs and dlls
  File /r "${SANDBOX}\*.jar"    ;copy back all the jar files
  File /r "${JREPATH}" ;copy the jre in

  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"

  ;create the start menu items
  CreateDirectory "$SMPROGRAMS\Portico-${VERSION}"
  CreateShortCut "$SMPROGRAMS\Portico-${VERSION}\README.lnk" "$INSTDIR\README.txt"
  CreateShortCut "$SMPROGRAMS\Portico-${VERSION}\Portico Web Site.lnk" "$INSTDIR\Portico.url"
  CreateShortCut "$SMPROGRAMS\Portico-${VERSION}\Portico User Documentation.lnk" "$INSTDIR\PorticoUserDocumentation.url"
  CreateShortCut "$SMPROGRAMS\Portico-${VERSION}\Uninstall Portico.lnk" "$INSTDIR\uninstall.exe"

SectionEnd

;--------------------------------
;Descriptions

  ;Language strings
  ;LangString DESC_SecDummy ${LANG_ENGLISH} "A test section."

  ;Assign language strings to sections
  ;!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  ;  !insertmacro MUI_DESCRIPTION_TEXT ${SecDummy} $(DESC_SecDummy)
  ;!insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;Uninstaller Section

Section "Uninstall"

  ;ADD YOUR OWN FILES HERE...
  Delete "$INSTDIR\Uninstall.exe"

  RMDir /r "$INSTDIR"

  ;delete the start menu items
  RMDir /r "$SMPROGRAMS\Portico-${VERSION}"

SectionEnd
