'''
Created on Feb 9, 2016
@author: zuozhi

a python module consists of several utility functions
includes: getWebpage, downloadFiles, parseLinks, getFiles
'''

import urllib.request
import traceback
from threading import Thread
from queue import Queue
from time import time
import os

PRINT_ON = True

# return the response of the given URL
def getWebpage(url: str):
    headers = {}
    headers['User-Agent'] = "Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.27 Safari/537.17"
    request = urllib.request.Request(url, headers = headers)
    response = urllib.request.urlopen(request)
    return response


# files_dict is a dictionary with path to save as key, and URL as value
# download and save all the links using multi-threading
def downloadFiles(files_dict: dict) -> None:
    THREAD_NUMBER = 16
    timeStart = time()
    
    class Downloader(Thread):
        def __init__(self, queue, num):
            Thread.__init__(self)
            self.queue = queue
            self.num = num
            
        def run(self):
            while True:
                [name, url] = self.queue.get()
                try:
                    global PRINT_ON
                    if (PRINT_ON):
                        print("thread {}: sending request to {}".format(self.num, url))
                    response = getWebpage(url)
                    with open(name, "w", encoding="utf-8") as writeFile:
                        writeFile.write(response.read().decode("utf-8"))
                except:
                    traceback.print_exc()
                self.queue.task_done()

    queue = Queue()
    for i in range(THREAD_NUMBER):
        downloader = Downloader(queue, i)
        downloader.daemon = True
        downloader.start()  
        
    for entry in files_dict.items():
        queue.put(entry)
        
    queue.join()
              
    print("all files saved")
    print("took {:.2f} seconds".format(time()-timeStart))
    

# files is a list of all files that needs to be parsed
# parseFunc is the function applied to each file
# results is an empty list that holds all the results
def parseLinks(files, parseFunc, results):
    print("parsing links file")
    THREAD_NUMBER = 8
    timeStart = time()
    
    class LinkParser(Thread):
        def __init__(self, queue, num):
            Thread.__init__(self)
            self.queue = queue
            self.num = num
            
        def run(self):
            while True:
                file = self.queue.get()
                try:
                    global PRINT_ON
                    if (PRINT_ON):
                        print("thread {}: parsing link {}".format(self.num, file))
                    with open(file, "r", encoding="utf-8") as readFile:
                        parseFunc(readFile.read(), results)
                except:
                    traceback.print_exc()
                self.queue.task_done()
    
    queue = Queue()
    for i in range(THREAD_NUMBER):
        downloader = LinkParser(queue, i)
        downloader.daemon = True
        downloader.start()  
        
    for entry in files:
        queue.put(entry)
        
    queue.join()
              
    print("all links parsed")
    print("took {:.2f} seconds".format(time()-timeStart))
    

# getFilesList files all files in the given folder, return a list of files
def getFileList(folder):
    '''make the all_files_list contains all the files in that paths'''
    for i in os.listdir(folder):
        if os.path.isdir(os.path.join(folder, i)):
            try:
                getFileList(os.path.join(folder, i))
            except PermissionError:
                print('you don\'t have permissions to this directory', os.path.join(folder, i))
                print('Files in this directory will not be searched')
            except:
                print('unknown error')
        else:
            if i!= '.DS_Store':
                yield os.path.join(folder, i)