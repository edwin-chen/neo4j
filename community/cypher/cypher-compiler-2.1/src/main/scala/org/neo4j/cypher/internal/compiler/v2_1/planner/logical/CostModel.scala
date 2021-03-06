/**
 * Copyright (c) 2002-2014 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.internal.compiler.v2_1.planner.logical

import org.neo4j.cypher.internal.compiler.v2_1.planner.logical.plans.LogicalPlan

/*
The cost model calculates the cost for different operations. Costs follow a basic formula - there's a constant cost
for preparing things before a query can start running, and then there is a cost per row expected to pass through
an operator.

This module species the arithmetic formulas that are used to estimate the cost of execution plans. For every different
join method and for every different index type access and in general for every distinct kind of step that can be found
in an execution plan there is a formula that gives its cost. Given the complexity of many of these steps most of these
formulas are simple approximations of what the system actually does and are based on certain assumptions regarding
issues like buffer management, disk-cpu overlap, sequential vs random IO etc.*/

trait CostModel extends PlanMetric {
  final def cost(plan: LogicalPlan): Int = apply(plan)
}

class CachingCostModel(metric: CostModel) extends CachingPlanMetric[CostModel](metric) with CostModel

object CostModel {
  def lift(f: PartialFunction[LogicalPlan, Int]): CostModel = new CostModel {
    def apply(plan: LogicalPlan): Int = f.lift(plan).getOrElse(Int.MaxValue)
  }
}
