'''
Created on Feb 15, 2016

@author: zuozhi
'''

from bs4 import BeautifulSoup
import crawler_utils
import traceback

def getWebMDSymptomURLs():
    yield ("a_z", "http://symptomchecker.webmd.com/symptoms-a-z")


def downloadMainPages(file_dict):
    save_file = "./data/WebMD_symptoms/pages/webMD_symtoms_"
    for (name, url) in getWebMDSymptomURLs():
        name = name.replace("/", ":")
        file_dict[save_file+name+".html"] = url
    crawler_utils.downloadFiles(file_dict)


def parseWebMDSymptoms(responseStr, results):
    soup = BeautifulSoup(responseStr, "html.parser")
    for i in range(65, 91):
        try:
            alpha = soup.find(id = "list_"+chr(i))
            for child in alpha.descendants:
                if child.name == "a":
                    results[str(child.string)] = "http://symptomchecker.webmd.com/"+str(child["href"])
        except:
            traceback.print_exc() 


def parsePages(pages, results):
    crawler_utils.parseLinks(pages, parseWebMDSymptoms, results)            


def saveTitles(titles):
    with open("./data/WebMD_symptoms/WebMD_symptoms.txt","w", encoding = 'utf-8') as keywordsFile:
        for key in titles:
            print(key, file = keywordsFile)



def downloadLinks(file_dict, results):
    save_file = "./data/WebMD_symptoms/links/"
    link_dict = {}
    for (name, url) in results.items():
        name = name.replace("/", ":")
        link_dict[save_file+name+".html"] = url
    crawler_utils.downloadFiles(link_dict)






if __name__ == "__main__":
    file_dict = {}
    results = {}
    # you can comment out any function as you need
    
    # download the main web pages
    downloadMainPages(file_dict)
    
    
    # choose one parsePages from below
    # parse the web pages from the files_dict if you download again
    parsePages(file_dict.keys(), results)
    
    # parse the web pages from local files if you already download them
    #parsePages(crawler_utils.getFileList("./data/WebMD_drugs/pages/"), results)
    
    # save the disease names to a file
    saveTitles(results.keys())
    
    
    # download the links from main pages, using parsed result
    downloadLinks(file_dict, results)