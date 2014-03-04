@echo "Updating .xml"

@echo off
for /r %%F in (*.xml) do (
  type "%%F"|repl "android:minSdkVersion=\q17\q" "android:minSdkVersion=\q15\q" LX >"%%F.new"
  move /y "%%F.new" "%%F" >nul
)

@echo on
@echo "Updating project.properties"
@echo off
for /r %%F in (*roject.properties) do (
  type "%%F"|repl "target=android-17" "target=android-15" LX >"%%F.new"
  move /y "%%F.new" "%%F" >nul
)

@echo on
@echo "Updating build.gradle"
@echo off
for /r %%F in (*uild.gradle) do (
  type "%%F"|repl "compileSdkVersion 19" "compileSdkVersion 15" LX >"%%F.new"
  type "%%F.new"|repl "compileSdkVersion 17" "compileSdkVersion 15" LX >"%%F.new2"
  type "%%F.new2"|repl "buildToolsVersion \q19.0.2\q" "buildToolsVersion \q19.0.1\q" LX >"%%F.new3"
  move /y "%%F.new3" "%%F.new2" >nul
  move /y "%%F.new2" "%%F.new" >nul
  move /y "%%F.new" "%%F" >nul
)