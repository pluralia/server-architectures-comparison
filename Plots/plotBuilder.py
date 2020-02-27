import matplotlib.pyplot as plt

# Blocked: one-client-one-thread
# X | N | D
# 4 10000 0
# M | RESPONSE_TIME | TASK_ON_SERVER | CLIENT_ON_SERVER
# 8 1378 1328 1345
# 12 3125 3082 3089
# 16 2790 2739 2752

tipe_names = []
changed_param = 0
fig, ax = plt.subplots(nrows=1, ncols=3)
for i in range(3):
    with open('output%i.txt' % (i+1)) as f:
        lines = f.readlines()
        tipe_names.append(lines[0])
        rows_name = lines[3].split(" | ")
        print(rows_name)
        lines = lines[4:]
        METRIC = [float(line.split()[0]) for line in lines]
        T1 = [float(line.split()[1]) for line in lines]
        T2 = [float(line.split()[2]) for line in lines]
        T3 = [float(line.split()[3]) for line in lines]

    data = list(zip(METRIC, T1, T2, T3))
    sortedList = sorted(data, key=lambda a: a[0])
    res = list(zip(*sortedList))

    ax[0].plot(res[0], res[1])
    ax[1].plot(res[0], res[2])
    ax[2].plot(res[0], res[3])

ax[0].legend(tipe_names, shadow=True)
ax[0].grid(False)
ax[0].set_xlabel(rows_name[0])
ax[0].set_ylabel(rows_name[1])

ax[1].grid(False)
ax[1].set_xlabel(rows_name[0])
ax[1].set_ylabel(rows_name[2])

ax[2].grid(False)
ax[2].set_xlabel(rows_name[0])
ax[2].set_ylabel(rows_name[3][:-1])

plt.show()

