import time
import os

input_file = "../MedExtraction/resources/dataset/ipubmed/ipubmed_abs_present.json"

output_path = "../MedExtraction/resources/dataset/temp/"
output_files = []
# output_files.append([10000, "abstract_10K/"])
# output_files.append([50000, "abstract_50K/"])
# output_files.append([100000, "abstract_100K/"])
# output_files.append([500000, "abstract_500K/"])
# output_files.append([1000000, "abstract_1M/"])
# output_files.append([2000000, "abstract_2M/"])

output_files.append([200000, "abstract_200K/"])
output_files.append([300000, "abstract_300K/"])
output_files.append([400000, "abstract_400K/"])

for i in output_files:
    output_lines = i[0]
    output_files = output_path+i[1]
    if not os.path.exists(output_files):
        os.makedirs(output_files)
    print("start: ", output_files)
    start = time.time()
    last_line = None
    with open(input_file, "r", encoding = "utf-8") as inFile:
        counter = 1
        while (counter <= output_lines):
            current_line = inFile.readline()
            if (current_line == None):
                break;
            else:
                output_file = output_files+str(counter)
                with open(output_file, "w", encoding = "utf-8") as outFile:
                    if len(current_line) > 1500:
                        crop_len = 1500
                        current_pos = 0
                        while (current_pos+crop_len < len(current_line)):
                            outFile.write(current_line[current_pos:current_pos+crop_len]+"\n")
                            current_pos += crop_len
                        outFile.write(current_line[current_pos:]);
                    else:

                        outFile.write(current_line)
                counter += 1
        print(counter)
    end = time.time()
    print("time: ", end - start)

