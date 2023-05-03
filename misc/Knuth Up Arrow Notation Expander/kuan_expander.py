def recursion(b,n):
	if len(n) == 1:
		# base case
		if n[0][0] == 1:
			return b ** n[0][1]
		# expand out single expression to step down hyperoperation sequence
		else:
			f, i = n[0][0]-1, n[0][1]-1
			n = []
			for x in range(i):
				n.append([f,b])
	# collapse down expanded expression
	else:
		i = len(n)-1
		for x in range(i):
			c = [n.pop()]
			n[-1][1] = recursion(n[-1][1],c)
	return recursion(b,n)

def main():
	d = 80
	print("\nKnuth Up Arrow Notation Decimal Expander")
	b = int(input("  Enter base: "))
	k = int(input("  Enter number of arrows: "))
	a = int(input("  Enter argument: "))
	v = recursion(b,[[k,a]])
	l = 0
	if v > 10**d:
		while v > 0:
			l += 1
			v //= 10
	print(f"\n{b}{'^'*k}{a} =\n\n{f'{l} digits' if l >= d else v}\n")

main()