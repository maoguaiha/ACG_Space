@echo off
cd C:\ProgramFiles\Program\java\ACG_Space\backend
call mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
set /p CP=<cp.txt
java -cp "target/classes;%CP%" com.ruoyi.project.AcgSpaceApplication