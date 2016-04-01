'''
Created on Feb 14, 2016

@author: zuozhi
'''

from bs4 import BeautifulSoup
import crawler_utils
import traceback


def getWebMDDrugURLS():
    atoz = [chr(i) for i in range(65, 91)] + ["0"]
    for alpha in atoz:
        for sub in range(1, 6):
            yield (str(alpha)+str(sub), "http://www.webmd.com/drugs/index-drugs.aspx?alpha={}&subTab={}&show=drugs".format(alpha, sub))

def parseWebMDDrugs(responseStr, results):
    soup = BeautifulSoup(responseStr, "html.parser")
    az_box = soup.find(id = "az-box")
    try:
        for i in az_box.contents[1].contents[1]:
            if i.string == None:
                element = i.find("a")
                results[str(element.string)] = "http://www.webmd.com"+str(element["href"])
        for i in az_box.contents[1].contents[3]:
            if i.string == None:
                element = i.find("a")
                results[str(element.string)] = "http://www.webmd.com"+str(element["href"])
    except:
        traceback.print_exc()


def downloadMainPages(file_dict):
    save_file = "./data/WebMD_drugs/pages/webMD_"
    for (name, url) in getWebMDDrugURLS():
        name = name.replace("/", ":")
        file_dict[save_file+name+".html"] = url
    crawler_utils.downloadFiles(file_dict)


def downloadLinks(file_dict, results):
    save_file = "./data/WebMD_drugs/links/"
    link_dict = {}
    for (name, url) in results.items():
        name = name.replace("/", ":")
        link_dict[save_file+name+".html"] = url
    crawler_utils.downloadFiles(link_dict)


def parsePages(pages, results):
    crawler_utils.parseLinks(pages, parseWebMDDrugs, results)


def saveTitles(titles):
    titles.sort()
    with open("./data/WebMD_drugs/WebMD_drugs.txt","w", encoding = 'utf-8') as keywordsFile:
        for key in titles:
            print(key, file = keywordsFile)


if __name__ == "__main__":
    file_dict = {}
    results = {}
    # you can comment out any function as you need
    
    # download the main web pages
    #downloadMainPages(file_dict)
    
    
    # choose one parsePages from below
    # parse the web pages from the files_dict if you download again
    #parsePages(file_dict.keys(), results)
    
    # parse the web pages from local files if you already download them
    parsePages(crawler_utils.getFileList("./data/WebMD_drugs/pages/"), results)
    
    
    # save the disease names to a file
    saveTitles(list(results.keys()))
    
    
    # download the links from main pages, using parsed result
    #downloadLinks(file_dict, results)