(define (domain domain0)
	(:types xpos ypos value)
	(:predicates
		(wall ?x ?y)
		(box ?x ?y)
		(avatar ?x ?y)
		(hole ?x ?y)
		(suc-x ?x ?y)
		(suc-y ?x ?y)
		(iterator ?i ?i)
		(box_counter ?i)
	)
	(:action right
		:parameters (
			?x0 ?x1 ?x2 
			?y
			?v0 ?v1
		)
		:preconditions
		(and
			(avatar ?x0 ?x0)
			(iterator ?i0 ?i1)
			(suc ?x0 ?x1)
		)
		:effect
			(when (not (wall ?x1 ?x0))
			(avatar ?x0 ?x0)

