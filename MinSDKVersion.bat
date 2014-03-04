@echo off
for /r %%F in (*.xml) do (
  type "%%F"|repl "android:minSdkVersion=\q17\q" "android:minSdkVersion=\q15\q" LX >"%%F.new"
  move /y "%%F.new" "%%F" >nul
)

for /r %%F in (project.properties) do (
  type "%%F"|repl "target=android-17" "target=android-15" LX >"%%F.new"
  move /y "%%F.new" "%%F" >nul
)