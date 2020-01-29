import matplotlib.pyplot as plt

tipe_names = []
changed_param = 0
fig, ax = plt.subplots(nrows=1, ncols=3)
for i in range(3):
    # with open('unblocked.txt') as f:
    with open('output%i.txt' % (i+1)) as f:
        lines = f.readlines()
        tipe_names.append(lines[0])
        rows_name = lines[1].split(' ')
        lines = lines[2:]
        X = [float(line.split()[0]) for line in lines]
        N = [float(line.split()[1]) for line in lines]
        M = [float(line.split()[2]) for line in lines]
        D = [float(line.split()[3]) for line in lines]
        T1 = [float(line.split()[4]) for line in lines]
        T2 = [float(line.split()[5]) for line in lines]
        T3 = [float(line.split()[6]) for line in lines]

    data = list(zip(X, N, M, D, T1, T2, T3))
    if i == 0:
        for j in range(4):
            if data[0][j] != data[1][j]:
                changed_param = j
                break
    sortedList = sorted(data, key=lambda a: a[changed_param])
    res = list(zip(*sortedList))

    ax[0].plot(res[changed_param], res[4])
    ax[1].plot(res[changed_param], res[5])
    ax[2].plot(res[changed_param], res[6])
    # ax[0].plot(res[changed_param], [d + (.1 * d * d - d * 10) * i for d in res[4]])
    # ax[1].plot(res[changed_param], [d + (.1 * d * d - d * 10) * i for d in res[5]])
    # ax[2].plot(res[changed_param], [d + (.1 * d * d - d * 10) * i for d in res[6]])

ax[0].legend(tipe_names, shadow=True)
ax[0].grid(False)
ax[0].set_xlabel(rows_name[changed_param])
ax[0].set_ylabel(rows_name[4])

ax[1].grid(False)
ax[1].set_xlabel(rows_name[changed_param])
ax[1].set_ylabel(rows_name[5])

ax[2].grid(False)
ax[2].set_xlabel(rows_name[changed_param])
ax[2].set_ylabel(rows_name[6])

plt.show()