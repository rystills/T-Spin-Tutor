import tkinter as tk
import pyautogui
import time
import threading

class TkApp(threading.Thread):
    def __init__(self):
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
        self.root.geometry('1920x1080+0+0')
        self.root.mainloop()

def main():
    #initialize an always on top, semitransparent window with no decorations, that covers the entire screen
    app = TkApp()
    #test screenshot abilities
    time.sleep(4)
    sc = pyautogui.screenshot()
    print(sc.getpixel((760,223)))
    
if __name__ == "__main__":
    main()