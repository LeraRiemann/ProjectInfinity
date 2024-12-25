import random
import math
from PIL import Image

perm = [i for i in range(256)]
random.seed(0)
random.shuffle(perm)
perm += perm
dirs = [(math.cos(a * 2.0 * math.pi / 256),
         math.sin(a * 2.0 * math.pi / 256))
         for a in range(256)]

def noise(x, y, z, per, perZ):
    def surflet(gridX, gridY, gridZ):
        distX, distY, distZ = abs(x-gridX), abs(y-gridY), abs(z-gridZ)
        polyX = 1 - 6*distX**5 + 15*distX**4 - 10*distX**3
        polyY = 1 - 6*distY**5 + 15*distY**4 - 10*distY**3
        polyZ = 1 - distZ
        hashed = perm[perm[perm[int(gridX) % per] + int(gridY) % per] + int(gridZ) % perZ]
        grad = (x-gridX)*dirs[hashed][0] + (y-gridY)*dirs[hashed][1]
        return polyX * polyY * polyZ * grad
    intX, intY, intZ = int(x), int(y), int(z)
    return (surflet(intX+0, intY+0, intZ+0) + surflet(intX+1, intY+0, intZ+0) +
            surflet(intX+0, intY+1, intZ+0) + surflet(intX+1, intY+1, intZ+0) +
            surflet(intX+0, intY+0, intZ+1) + surflet(intX+1, intY+0, intZ+1) +
            surflet(intX+0, intY+1, intZ+1) + surflet(intX+1, intY+1, intZ+1)) 

def fBm(x, y, z, per, perZ, octs):
    val = 0
    for o in range(octs):
        val += 0.5**o * noise(x*(2**o), y*(2**o), z*(2**o), per*(2**o), perZ*(2**o))
    return val

size, sizeZ = 16, 60
freq, freqZ, octs, data, data1 = 1/16.0, 1/60.0, 5, [], []
for z in range(sizeZ):
    for y in range(size):
        for x in range(size):
            i = fBm(x*freq, y*freq, z*freqZ, int(size*freq), int(sizeZ*freqZ), octs) #0 to 1
            #data.append((int(192 + 64*i), 224))

            hue = z/sizeZ + i/4.0
            hue -= int(hue)
            wh = int(6*hue)
            rem = 6*hue - wh

            col1 = int(256*rem)
            col2 = int(256*(1-rem))

            if (wh == 0): data1.append((255, col1, 0, 255))
            if (wh == 1): data1.append((col2, 255, 0, 255))
            if (wh == 2): data1.append((0, 255, col1, 255))
            if (wh == 3): data1.append((0, col2, 255, 255))
            if (wh == 4): data1.append((col1, 0, 255, 255))
            if (wh == 5): data1.append((255, 0, col2, 255))
#im = Image.new("LA", (size, size*sizeZ)) #light+alpha, not what you thought about
#im.putdata(data)
#im.save("iridescence.png")
im1 = Image.new("RGBA", (size, size*sizeZ))
im1.putdata(data1)
im1.save("iridescent/block.png")