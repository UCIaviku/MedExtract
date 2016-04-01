'''
Created on Feb 9, 2016

@author: zuozhi
'''

from bs4 import BeautifulSoup
import crawler_utils
import traceback

def getNellURLs():
    start = [5000 * i for i in range(4)]
    for s in start:
        yield (str(s), "http://rtw.ml.cmu.edu/rtw/kbbrowser/list.php?pred=disease&start={}".format(s))
        
def parseNellDiseases(responseStr, results):
    soup = BeautifulSoup(responseStr, "html.parser")
    body = soup.find(id = "list")

    try:
        table = None
        for child in body.contents:
            if child.name == "table" and ("instance" in child["class"]):
                table = child
                break
        if table == None:
            print("table is none")
        if table != None:
            for tr in table.children:
                if (tr.name == "tr" and ("instance" in child["class"])):
                    for td in tr.children:
                        if td.name == "td" and ("instance" in td["class"]):
                            if (str(td.a["href"]).startswith("./")):
                                results[str(td.a.string)] = "http://rtw.ml.cmu.edu/rtw/kbbrowser/entity.php?id="+str(td.a["href"])[2:]
                            else:
                                results[str(td.a.string)] = str(td.a["href"])
    except:
        traceback.print_exc()

    
def downloadMainPages(file_dict):
    save_file = "./data/NELL/pages/NELL_"
    for (name, url) in getNellURLs():
        name = name.replace("/", ":")
        file_dict[save_file+name+".html"] = url
    crawler_utils.downloadFiles(file_dict)
    

def parsePages(results):
    print("parsing pages")
    folder = "./data/NELL/pages"
    for page in crawler_utils.getFileList(folder):
        with open(page, "r", encoding = "utf-8") as readFile:
            parseNellDiseases(readFile.read(), results)

       
def saveTitles(results):
    print("saving titles")
    with open("./data/NELL/titles.txt","w") as keywordsFile:
        for key in results.keys():
            print(key, file = keywordsFile)  
    with open("./data/NELL/links.txt", "w") as linksFile:
        for (title,url) in results.items():
            print(title+" | "+url, file = linksFile)
        
    
def downloadLinks(file_dict, results):
    save_file = "./data/NELL/links/"
    link_dict = {}
    for (name, url) in results.items():
        name = name.replace("/", ":")
        link_dict[save_file+name+".html"] = url
    crawler_utils.downloadFiles(link_dict)
    

def parseLinkFunc(fileStr, results):
    try:
        soup = BeautifulSoup(fileStr, "html.parser")
        body = soup.find(id = "entity")
        if body != None and body.p.a != None:
            literal = str(body.p.a.string)
            if '?' not in literal:
                results.append(literal)
    except:
        traceback.print_exc()
        

def getDiseaseLiterals(files):
    literalsFile = "./data/NELL/disease_literals.txt"
    results = []
    crawler_utils.parseLinks(files, parseLinkFunc, results)
    with open (literalsFile, "w", encoding = "utf-8") as writeFile:
        for i in results:
            print(i, file = writeFile)
            

def parseConfidence(responseStr, confidenceResults):
    soup = BeautifulSoup(responseStr, "html.parser")
    body = soup.find(id = "list")
    try:
        table = None
        for child in body.contents:
            if child.name == "table" and ("instance" in child["class"]):
                table = child
                break
        if table != None:
            tr_count = 0
            for tr in table.children:
                try:
                    if tr.name == "tr":
                        tr_count += 1
                        if tr_count != 1:
                            td_count = 0
                            for td in tr.children:
                                if td.name == "td":
                                    td_count += 1
                                    if td_count == 1:
                                        name = td.a.string
                                    elif td_count == 4:
                                        try:
                                            confidence = float(td.string)
                                        except:
                                            confidence = 100.00
                            confidenceResults[name] = confidence
                except:
                    traceback.print_exc()   
    except:
        traceback.print_exc()

def compare_confidence(file_name, confidenceResults):
    name = file_name[18:-5]
    if (name in confidenceResults):    
        return confidenceResults[name] == 100
    else:
        return False


def findConfidenceResults():
    confidenceResults = {}
    for page in crawler_utils.getFileList("./data/NELL/pages"):
        with open(page, "r", encoding = "utf-8") as pageFile:
            parseConfidence(pageFile.read(), confidenceResults)
    print(confidenceResults)
    links = crawler_utils.getFileList("./data/NELL/links")
    links_100_confidence = [link for link in links if compare_confidence(link, confidenceResults)]
    print(links_100_confidence)
    getDiseaseLiterals(links_100_confidence)
    
        
        
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
    #parsePages(crawler_utils.getFileList("./data/WebMD_drugs/pages/"), results)
    
    # save the disease names to a file
    #saveTitles(results.keys())
    
    
    # download the links from main pages, using parsed result
    #downloadLinks(file_dict, results)
    
    # parse the pages download and get the disease name literals
    #getDiseaseLiterals(crawler_utils.getFileList("./data/NELL/links"))
    
    findConfidenceResults()
    