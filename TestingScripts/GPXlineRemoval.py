
f = open("59_Test_Track_1000_ms_i1.Gpx",'r')
Lines = f.readlines()
newLines = []
count = 0
for line in Lines:
    count+=1
    #print(count)
    #skip header and final line
    if count == 0 or count == 1 or count == (len(Lines)):
        newLines.append(line)
    #skip over the wrong lines
    elif 'lon' in line[12:30]:
        print(line[12:30])
    else:
        newLines.append(line)

newfile = open("59_Test_Track_1000_ms_i1_fixed.Gpx",'w') 
newfile.writelines(newLines)
