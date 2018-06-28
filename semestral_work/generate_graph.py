import pandas as pd
import matplotlib.pyplot as plt

headers = ['generation','best fitness','avrage fitness']
df = pd.read_csv('log.txt', names=headers)

x = df['generation']
y1 = df['best fitness']
y2 = df['avrage fitness']

f = plt.figure()

plt.plot(x, y1)
plt.plot(x, y2)
plt.yscale('logit')
plt.title('Vývoj fitness v průběhu generací')
plt.grid(True)

plt.xlabel('Generace')
plt.ylabel('Fitness')

plt.legend(['Nejlepší fitness', 'Průměrná hodnota fitness'], loc='upper left')

plt.show()

f.savefig("graph.pdf", bbox_inches='tight')
