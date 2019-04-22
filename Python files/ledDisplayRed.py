
from sense_hat import SenseHat
sense = SenseHat()
red = [255,0,0]
pixel_red = [red for i in range(64)]
sense.set_pixels(pixel_red)
