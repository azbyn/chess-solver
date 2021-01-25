# thing = """
# <Spinner
#     android:id="@+id/dropdown%d"
#     style="@style/BtnImage"
#     android:layout_column="%s"
#     android:layout_row="%s"
#     android:background="@android:drawable/btn_dropdown"
#     android:spinnerMode="dropdown" />
# """

# for i in range(64):
#     c = i % 8
#     r = i//8
#     if c!=r: continue
#     print(thing.strip() % (i, c, r))

import subprocess
for p in 'kqrbnp':
    for c in 'dl':
        url = rf"https://commons.wikimedia.org/wiki/File:Chess_{p}{c}t45.svg"
        subprocess.run(["firefox", url])
        thing =  input("WAIT:")
        subprocess.run(["wget", thing])
        
