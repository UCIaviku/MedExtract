'''
Created on Feb 15, 2016

@author: zuozhi
'''

from bs4 import BeautifulSoup
import crawler_utils
import traceback

def getWebMDDiseaseURLs():
    # default + b-z in lower case
    atoz = ["default"] + [chr(i) for i in range(98, 123)]
    for alpha in atoz:
        yield (alpha, "http://www.webmd.com/a-to-z-guides/health-topics/"+alpha+".htm")


def downloadMainPages(file_dict):
    save_file = "./data/WebMD_diseases/pages/WebMD_diseases_"
    for (name, url) in getWebMDDiseaseURLs():
        name = name.replace("/", ":")
        file_dict[save_file+name+".html"] = url
    crawler_utils.downloadFiles(file_dict)


def parseWebMDDiseases(responseStr, results):
    soup = BeautifulSoup(responseStr, "html.parser")
    contents = soup.find(id = "ContentPane8")
    try:
        if contents != None:
            ul = contents.div.ul
            for li in ul:
                if li.name == "li":
                    if (str(li.a["href"])[0]) == "/":
                        results[str(li.a.string)] = "http://www.webmd.com"+str(li.a["href"])
                    else:
                        results[str(li.a.string)] = "http://www.webmd.com/"+str(li.a["href"])
        else:
            print("Contents is None")
    except:
        traceback.print_exc() 



def parsePages(pages, results):
    crawler_utils.parseLinks(pages, parseWebMDDiseases, results)            


def saveTitles(titles):
    with open("./data/WebMD_diseases/WebMD_diseases.txt","w", encoding = 'utf-8') as keywordsFile:
        for key in titles:
            print(key, file = keywordsFile)



def downloadLinks(file_dict, results):
    save_file = "./data/WebMD_diseases/links/"
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