## **THIS PROGRAM COMES WITH NO WARRANTY. I AM NOT LIABLE FOR ANY ERRORS THE PROGRAM ENCOUNTERS.**

## **SAVE OFTEN AND MAKE BACKUPS.**

That being said, you can always create an issue on gitlab --> https://gitlab.com/zachbryant/CSE-154-Awesome-Homework-Tool

This tool was a side project I started because I'm lazy and like my menial tasks to be done for me.

## **HOW TO USE**

* Step 1. Navigate into the jar, open homeworktool.txt, and type the following IN THIS ORDER
        - UW Net ID
        - Net ID password
        - the directory you store all of your homework in. For example, "D:\mystuff\CSE 154\homework". This should have inside of it all of the folders hw1, hw2, hw..
        Make sure you save your credentials here. You can always update them if you need to.
        
* Step 2. Save the text file and exit. You will see a prompt asking to update the file in the archive. Say yes and move to step 3

* Step 3. Extract the .jar file, the lib folder, and hwtool.bat to C:\Users\<your user>\154\

* Step 4. Press windows+R, type cmd, then type '154\hwtool'. This will start the homework tool

* Step 5. Explore the commands. Use 'help' for info on what you can do specifically.

* Step 6. ?????

* Step 7. PROFIT!

## **NOTES**

* In step 2, adding the script to this folder enables you to run the script from command line just by typing '154\hwtool'. If you wish to rename the script, you can start it by typing the new name in cmd

* PLEASE DO NOT overwrite any existing folders in your users folder. This can lead to catastrophic failure in rare cases. Just rename to something else

* You don't need to type out the entire file path every time. That would defeat the purpose. You can just start your file path with 'def' and the program will assume you want to submit from the homework folder. If you don't specify a path aside from def, the program will look in its current directory e.g. def\hw6 -u