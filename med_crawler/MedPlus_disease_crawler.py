'''
Created on Feb 9, 2016

@author: zuozhi
'''

from bs4 import BeautifulSoup
import crawler_utils

def getMedPlusURLs():
    atoz = [chr(i) for i in range(65, 91)] + ["0-9"]
    for i in atoz:
        yield (i, "https://www.nlm.nih.gov/medlineplus/ency/encyclopedia_{}.htm".format(i))

       
def parseMedPlus(responseStr, results):
    soup = BeautifulSoup(responseStr, "html.parser")
    page_list = soup.find(id = "index")
    for i in page_list.contents:
        element = i.find("a")
        results[str(element.string)] = "https://www.nlm.nih.gov/medlineplus/ency/"+str(element["href"])


def downloadMainPages(file_dict):
    save_file = "./data/MedlinePlus/pages/medline_"
    for (name, url) in getMedPlusURLs():
        name = name.replace("/", ":")
        file_dict[save_file+name+".html"] = url
    crawler_utils.downloadFiles(file_dict)


def downloadLinks(file_dict, results):
    save_file = "./data/MedlinePlus/links/"
    link_dict = {}
    for (name, url) in results.items():
        name = name.replace("/", ":")
        link_dict[save_file+name+".html"] = url
    crawler_utils.downloadFiles(link_dict)


def parsePages(pages, results):
    crawler_utils.parseLinks(pages, parseMedPlus, results)


def saveTitles(titles):
    with open("./data/MedlinePlus/MedlinePlus_diseases.txt","w", encoding = 'utf-8') as keywordsFile:
        for key in titles:
            print(key, file = keywordsFile)
    print("MedelinePlus diseases saved")
    
def saveLinks(results):
    with open("./data/MedlinePlus/MedlinePlus_diseases_links.txt","w", encoding = 'utf-8') as linksFile:
        for (key, value) in results.items():
            print(key+" | "+value, file = linksFile)
    print("MedelinePlus diseases links saved")  


if __name__ == "__main__":
    file_dict = {}
    results = {}
    # you can comment out any function as you need
    
    # download the main web pages
    #downloadMainPages(file_dict)
    
    
    # choose one parsePages from below
    # parse the web pages from the files_dict
    #parsePages(file_dict.keys(), results)
    
    # parse the web pages from local files
    parsePages(crawler_utils.getFileList("./data/MedlinePlus/pages/"), results)
    
    # save the disease names to a file
    saveTitles(results.keys())
    
    # save the disease names and links to file
    saveLinks(results)
    
    
    # download the links from main pages, using parsed result
    #downloadLinks(file_dict, results)
    