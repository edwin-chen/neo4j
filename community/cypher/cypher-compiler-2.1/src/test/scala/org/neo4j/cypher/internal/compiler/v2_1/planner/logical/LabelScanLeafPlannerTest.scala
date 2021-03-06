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

import org.neo4j.cypher.internal.commons.CypherFunSuite
import org.neo4j.cypher.internal.compiler.v2_1.planner.logical.plans.{NodeByLabelScan, IdName}
import org.mockito.Mockito._
import org.neo4j.cypher.internal.compiler.v2_1.planner.{LogicalPlanningTestSupport, QueryGraph, Selections}
import org.neo4j.cypher.internal.compiler.v2_1.ast.LabelName
import org.neo4j.cypher.internal.compiler.v2_1.ast.Identifier
import org.neo4j.cypher.internal.compiler.v2_1.ast.HasLabels
import org.neo4j.cypher.internal.compiler.v2_1.{LabelId, DummyPosition}

class LabelScanLeafPlannerTest extends CypherFunSuite with LogicalPlanningTestSupport {

  private val pos = DummyPosition(0)

  test("simple label scan without compile-time label id") {
    // given
    val idName = IdName("n")
    val projections = Map("n" -> Identifier("n")(pos))
    val hasLabels = HasLabels(Identifier("n")(pos), Seq(LabelName("Awesome")()(pos)))(pos)
    val qg = QueryGraph(projections, Selections(Seq(Set(idName) -> hasLabels)), Set(idName), Set.empty)

    implicit val context = newMockedLogicalPlanContext(queryGraph = qg,
      estimator = CardinalityEstimator.lift {
        case _: NodeByLabelScan => 1
      })

    // when
    val resultPlans = labelScanLeafPlanner(Map(idName -> Set(hasLabels)))()

    // then
    resultPlans should equal(Seq(NodeByLabelScan(idName, Left("Awesome"))()))
  }

  test("simple label scan with a compile-time label ID") {
    // given
    val idName = IdName("n")
    val projections = Map("n" -> Identifier("n")(pos))
    val labelId = LabelId(12)
    val hasLabels = HasLabels(Identifier("n")(pos), Seq(LabelName("Awesome")(Some(labelId))(pos)))(pos)
    val qg = QueryGraph(projections, Selections(Seq(Set(idName) -> hasLabels)), Set(idName), Set.empty)

    implicit val context = newMockedLogicalPlanContext(queryGraph = qg,
      estimator = CardinalityEstimator.lift {
        case _: NodeByLabelScan => 100
      })
    when(context.planContext.indexesGetForLabel(12)).thenReturn(Iterator.empty)

    // when
    val resultPlans = labelScanLeafPlanner(Map(idName -> Set(hasLabels)))()

    // then
    resultPlans should equal(Seq(NodeByLabelScan(idName, Right(labelId))()))
  }
}
