import tkinter as tk
import pyautogui
import time
import threading
import random

#constants
sw = 1920
sh = 1080
bSize = 36
bSizePrev = 28
bSizePrevFuture = 22
gridLeft = 308
gridRight = 632
gridTop = 160
gridBot = 844
numRows = 20
numCols = 10

class TkApp(threading.Thread):
    def __init__(self):
        #init canvas to None so we can safely avoid a data race
        self.canvas = None
        threading.Thread.__init__(self)
        self.start()

    def callback(self):
        self.root.quit()

    def run(self):
        self.root = tk.Tk()
        self.root.protocol("WM_DELETE_WINDOW", self.callback)
        self.root.wait_visibility(self.root)
        self.root.wm_attributes("-topmost", 1)
        self.root.wm_attributes('-alpha', .1)
        self.root.overrideredirect(1)
        self.root.geometry('{0}x{1}+0+0'.format(sw,sh))
        
        self.canvas = tk.Canvas(self.root, width=sw, height=sh)
        self.canvas.pack()
                
        self.root.mainloop()

def main():
    #initialize an always on top, semitransparent TK window with no decorations, that covers the entire screen
    app = TkApp()
    #test screenshot abilities
    while (True):
        #stall until the tk app is initialized
        if (app.canvas == None):
            continue
        sc = pyautogui.screenshot()
        sc.getpixel((760,223))
        app.canvas.delete("all")
        x = gridLeft + bSize*random.randint(0,numCols-1)
        y = gridTop + bSize*random.randint(0,numRows-1)
        app.canvas.create_rectangle(x, y, x+bSize, y+bSize, fill='purple')
    
if __name__ == "__main__":
    main()