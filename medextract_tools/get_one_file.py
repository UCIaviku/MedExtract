import time



input_file = "/Users/georgewang/Documents/Project_Medextract/dataset/ipubmed/ipubmed_abs_present.json"

output_path = "/Users/georgewang/Documents/Project_Medextract/dataset/"
output_files = []
output_files.append([10000, "abstract_10K.txt"])
output_files.append([20000, "abstract_20K.txt"])
output_files.append([50000, "abstract_50K.txt"])
# output_files.append([100000, "abstract_100K.txt"])
# output_files.append([500000, "abstract_500K.txt"])
# output_files.append([1000000, "abstract_1M.txt"])
# output_files.append([2000000, "abstract_2M.txt"])

# output_files.append([250000, "abstract_250K.txt"])
# output_files.append([400000, "abstract_400K.txt"])
# output_files.append([550000, "abstract_550K.txt"])
# output_files.append([700000, "abstract_700K.txt"])
# output_files.append([700000, "abstract_850K.txt"])

# output_files.append([200000, "abstract_200K.txt"])
# output_files.append([300000, "abstract_300K.txt"])
# output_files.append([400000, "abstract_400K.txt"])

for i in output_files:
    output_lines = i[0]
    output_file = output_path+i[1]
    print(output_lines, "start")
    start = time.time()
    with open(input_file, "r", encoding = "utf-8") as inFile:
        with open(output_file, "w", encoding = "utf-8") as outFile:
            counter = 1
            while (counter <= output_lines):
                outFile.write(inFile.readline())
                counter += 1
    end = time.time()
    print("time: ", end - start)
             

