from sense_hat import SenseHat
sense = SenseHat()
green = [0,255,0]
pixel_green = [green for i in range(64)]
sense.set_pixels(pixel_green)
