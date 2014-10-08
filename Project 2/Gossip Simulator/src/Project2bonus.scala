import akka.actor._
import scala.math._
import scala.util.Random
import scala.concurrent.duration._
//import scala.concurrent.ExecutionContext.Implicits.global
//import network_package._
//import node_package._
//import akka.actor.Actor._

sealed trait Message
// MakeNodes Was used earlier for Initialization, but due to scope problem's I changed the method ( is a bit repetative now )
//case object MakeNodes extends Message

// Used to initialize the network according to the topology
case class Initialize (uniqueId:Int, neighbor:List[Int], all_network_nodes:List[ActorRef], master:ActorRef) extends Message

// Message sent to the master with the node reference and the algorithm to be used
case class GossipMessage (network_node:ActorRef, algorithm:String, numNodes:Int , failure_Nodes : Int) extends Message

//Gossip Algorithm
case class GossipAlgorithm (id:Int, start_time:Long , failureNodes : List[Int]) extends Message

// Push Sum algorithm
case class PushsumAlgorithm (sum:Double, weight:Double, starttim:Long , failureNodes : List[Int]) extends Message

// Used for Termination in gossip algorithm
case class Terminator (myID:Int, gossipCount:Int, start_time:Long , fail_Nodes : Int) extends Message

case class NextRound(myID:Int , start_time:Long , node_with_rumour : List[Int] , failureNodes : List[Int]) extends Message

// Main object
object Project2bonus extends App{
	
	var numNodes = 0							// Number of Nodes
	var topology = ""							// Topology Initialization
	var algorithm= ""							// Algorithm
	var rows : Double = 0						// Basically the square Root
	var gossip_after_topology_creation = 0		// Was used for Control Organization // Dropped_No use
	var failNodes : Int = 0

	// Checking the argument length

	if(args.length != 4)
	{
		println("************************Invalid Input**********************\n" + 
				"Input should be of type : \n scala project2 numNodes topology algorithm Num_FailingNodes\n" +
				"***********************************************************")
		System.exit(1)
	}
	// If the argument number is fine, Proceed into else
	else
	{
		numNodes  = args(0).toInt				// Converting number of node to Integer value
		topology  = args(1)						// Topology, String value
		algorithm = args(2)						// Algorithm, String value
		failNodes = args(3).toInt				// Number of nodes to fail

		//println("Input : scala project2 " + numNodes + " " + topology + " " + algorithm + " ") //Testing
		
		// Making an Actor System, Master and a list of nodes
		val system = ActorSystem("GossipSystem")
		var master : ActorRef =  system.actorOf(Props[Master], name = "master")
		var node : List[ActorRef] = Nil

		// Match the topology and make the Network
		topology match {
		
		//In case of full network
		case "full"  => {
							println(topology + " Topology ")
							//---------------------------------------------------------------
							//MakeNodes(numNodes)
							var node_loop : Int = 0
							while (node_loop < numNodes){
								//println("numNodes =" , numNodes)
								node ::= system.actorOf(Props[Nodes]) // ActorClass => Nodes
								node_loop += 1;
							}
							//---------------------------------------------------------------
							var neighbor_loop : Int = 0;
							while(neighbor_loop < node.length){
								
								var neighbors : List[Int] = Nil
								
								var i:Int = 0
								while (i < node.length){
									if (i != neighbor_loop) {
										neighbors ::= i
									}
									i += 1
								}		

								node(neighbor_loop) !  Initialize(neighbor_loop , neighbors, node , master)

								neighbor_loop += 1
							}
							gossip_after_topology_creation = 1
						}

		//In case of line network
		case "line"  => {
							println(topology + " Topology ")
							//---------------------------------------------------------------
							//MakeNodes(numNodes)
							var node_loop : Int = 0
							while (node_loop < numNodes){
								//println("numNodes =" , numNodes)
								node ::= system.actorOf(Props[Nodes]) // ActorClass => Nodes
								node_loop += 1;
							}
							//---------------------------------------------------------------
							//println("numNodes ="+ node.length)
							// Neighbor Loop
							var neighbor_loop : Int = 0;
							while(neighbor_loop < node.length){
								
								var neighbors : List[Int] = Nil
								
								if(neighbor_loop > 0)
									neighbors ::= (neighbor_loop - 1)
								if(neighbor_loop < (node.length - 1))
									neighbors ::= (neighbor_loop + 1)

								node(neighbor_loop) !  Initialize(neighbor_loop, neighbors, node , master)

								neighbor_loop += 1
							}
							
						gossip_after_topology_creation = 1	
						}

		//In case of 2D network
		case "2D" 	 => {
							println(topology + " Topology ")
							numNodes= SquareIt(numNodes)
							rows = math.sqrt(numNodes.toDouble)
							//---------------------------------------------------------------
							//MakeNodes(numNodes)
							var node_loop : Int = 0
							while (node_loop < numNodes){
								//println("numNodes =" , numNodes)
								node ::= system.actorOf(Props[Nodes]) // ActorClass => Nodes
								node_loop += 1;
							}
							//---------------------------------------------------------------
							//println("numNodes ="+ node.length)
							var neighbor_loop : Int = 0;
							while(neighbor_loop < node.length){
								
								var neighbors : List[Int] = Nil
								var left = neighbor_loop - 1
								var right = neighbor_loop + 1
								var top = neighbor_loop - rows
								var bottom = neighbor_loop + rows

								if (top >= 0) 
									neighbors ::= top.toInt
								if (bottom < numNodes) 
									neighbors ::= bottom.toInt
								if (neighbor_loop%rows > 0) 
									neighbors ::= left
								if (neighbor_loop%rows < (rows-1)) 
									neighbors ::= right

								node(neighbor_loop) !  Initialize(neighbor_loop,  neighbors, node , master)

								neighbor_loop += 1
							}
							gossip_after_topology_creation = 1

						}

		//In case of imp2D network
		case "imp2D" => {
							println(topology + " Topology ")
							numNodes= SquareIt(numNodes)
							rows = math.sqrt(numNodes.toDouble)
							//---------------------------------------------------------------
							//MakeNodes(numNodes)
							var node_loop : Int = 0
							while (node_loop < numNodes){
								//println("numNodes =" , numNodes)
								node ::= system.actorOf(Props[Nodes]) // ActorClass => Nodes
								node_loop += 1;
							}
							//---------------------------------------------------------------
							//println("numNodes ="+ node.length)
							var neighbor_loop : Int = 0;
							while(neighbor_loop < node.length){
								
								var neighbors : List[Int] = Nil
								var RandomNeighbor : Int = -1
								var left = neighbor_loop - 1
								var right = neighbor_loop + 1
								var top = neighbor_loop - rows
								var bottom = neighbor_loop + rows

								if (top >= 0) 
									neighbors ::= top.toInt
								if (bottom < numNodes) 
									neighbors ::= bottom.toInt
								if (neighbor_loop%rows > 0) 
									neighbors ::= left
								if (neighbor_loop%rows < (rows-1)) 
									neighbors ::= right

									while(RandomNeighbor == -1){
										RandomNeighbor = Random.nextInt(node.length)
										for(i <- neighbors){
											if(RandomNeighbor == i){
												RandomNeighbor = -1
											}
										}
									}
								neighbors ::= (RandomNeighbor)
								node(neighbor_loop) !  Initialize(neighbor_loop,  neighbors, node, master)

								neighbor_loop += 1
							}
							gossip_after_topology_creation = 1
						}
		//In all other cases
		case _ 		 => {
							println("No matching topology")
							println("The topologies you can use are : full , line , 2D, imp2D ")
							System.exit(1)
						}
		}	

		// For grid, we take the next perfect square
		def SquareIt (num_Nodes : Int): Int = {
			var square:Int = num_Nodes
			
			while(math.sqrt(square.toDouble)%1 != 0)
			{
				square += 1
			}
			return square
		}

		/*def MakeNodes (num_Nodes : Int)
		{
						val system = ActorSystem("GossipSystem")
						var master : ActorRef =  system.actorOf(Props[Master], name = "master") //MasteClass => Master
						var node : List[ActorRef] = Nil		//ActorList => Node

						var node_loop : Int = 0
						while (node_loop < num_Nodes){
							node ::= system.actorOf(Props[Nodes]) // ActorClass => Nodes
							node_loop += 1;
						}
		}*/

		// Match the algorithm and then call master
		/*if(gossip_after_topology_creation == 1){
		algorithm match {
		case "gossip" => println("Gossip")//new Gossip()
						 master ! "hi"
		
		case "pushsum" => println("PushSum")//new PushSum()
						  master ! "hi"
		
		case _ => {
					println("Sorry that Algorithm is under construction")
					println("The algorithms you can use are : gossip , pushsum")
					System.exit(1)
				  } 
	}
}*/
			master ! GossipMessage(node(0), algorithm, numNodes, failNodes)
	}

		class Master extends Actor{
			var Total_Nodes : Int = 0
			var Stop_Rumour : Int = 10
			var Nodes_Visited : List[Int] = Nil

			def receive={
				
				case "hi" => println("Reached Master")
				
				case GossipMessage (network_node:ActorRef, algorithm:String, numNodes:Int , failure_Nodes : Int) => {
					//println("Gossipping.....")
					// Randomly generating nodes that will fail
					var failureNodes : List[Int] = Nil
					var temp : Int = 0
					var i : Int = 0
					var count : Int = 0
					while(i < failure_Nodes){
						
						temp = Random.nextInt(numNodes)
						if(temp < numNodes && temp > 0)
						{	
							failureNodes ::= temp
							count += 1
						}
						i += 1 
					}
					println("Randomly Failing Nodes " + failureNodes)
					
					Total_Nodes = numNodes
					var start_time :Long = System.currentTimeMillis
					algorithm match {
					case "gossip" => {
										println("Gossiping...")//new Gossip()
										//master ! "hi"
										network_node ! GossipAlgorithm (-1, start_time, failureNodes)
					}
		
					case "pushsum" =>{
										println("PushSum...")//new PushSum()
										//master ! "hi"
										network_node ! PushsumAlgorithm (0, 1, start_time, failureNodes)
					} 
		
					case _ => {
								println("Sorry that Algorithm is under construction")
								println("The algorithms you can use are : gossip , pushsum")
								System.exit(1)
							 } 

					}
				}
				case Terminator (id:Int, gossipCount:Int, start_time:Long ,fail_Nodes : Int) => {
							
							var flag:Int = 1
							var i:Int = 0
							while(i < Nodes_Visited.length) {
									if(Nodes_Visited(i) == id) {
										flag = 0
									}
									i += 1
												}
					
												if (flag == 1) {
									Nodes_Visited ::= id
												}
					
												if(Nodes_Visited.length == (Total_Nodes - fail_Nodes)) {
									
									println("\nThe nodes that were visited are : ")
									var i:Int = 0
									while (i < Nodes_Visited.length) {
										if(Nodes_Visited(i) < Total_Nodes)
										print(Nodes_Visited(i)+"-->")
										i += 1
									}
								
									println("\n Time = " + (System.currentTimeMillis - start_time) + "ms")
									context.system.shutdown()
									System.exit(1)
							}
				}
				case _ => println("Hi") 
			}

		}
		class Nodes extends Actor{
			var master:ActorRef = null
			var neighbors:List[Int] = Nil
			var Count_Rumour:Int = 0
			var Delta_Difference:Int = 0
			var Stop_Rumour = 10
			var myID:Int = 0
			var node:List[ActorRef] = Nil
			var all_network_nodes:List[Int] =Nil
			var node_with_rumour:List[Int] = Nil
			var s_value:Double = 0
			var w_value:Double = 0
			var next_round_timer: Cancellable = _
			val system = context.system
			
			def receive={
				case Initialize(uniqueId:Int,  neighborList:List[Int], all_network_nodes:List[ActorRef], master_ref:ActorRef) => {
					//println("Reached Initialize");
					neighbors = neighbors ::: neighborList
					myID = uniqueId
					master = master_ref
					s_value = uniqueId
					node = all_network_nodes
				}
				case GossipAlgorithm(callerid:Int, start_time:Long , failureNodes : List[Int]) => {
					//println("Reached Gossip Algorithm")
					if ((Count_Rumour < Stop_Rumour) && (!(failureNodes contains myID)) ){
						//println(myID);
						if (callerid != myID) {

							Count_Rumour += 1
							master ! Terminator(myID, Count_Rumour, start_time , failureNodes.length)
						}

						//import system.dispatcher
						//next_round_timer = context.system.scheduler.schedule(0 milliseconds,0 milliseconds,self,NextRound(myID, start_time , node_with_rumour , failureNodes))
							
						// Can change the time of delay
						var RandomNeighbor:Int = 0
						RandomNeighbor = Random.nextInt(neighbors.length)
						node(neighbors(RandomNeighbor)) ! GossipAlgorithm(myID, start_time , failureNodes )
						self ! GossipAlgorithm(myID, start_time , failureNodes)
						
					}
					else {
							//println("I'm Dead " + myID)
							context.stop(self)
						}
					}

				case NextRound (myID : Int , start_time : Long , node_with: List[Int] , failureNodes : List[Int]) => {
						//println("NextRound")
						var RandomNeighbor:Int = 0
						RandomNeighbor = Random.nextInt(neighbors.length)
						node(neighbors(RandomNeighbor)) ! GossipAlgorithm(myID, start_time , failureNodes )
						self ! GossipAlgorithm(myID, start_time , failureNodes)
					}
						/*RandomNeighbor = Random.nextInt(neighbors.length)
						//println("---------------------------------------------------")
						//println("Transmitting ----" + RandomNeighbor + "----" + myID)
						node(neighbors(RandomNeighbor)) ! GossipAlgorithm(myID, start_time)
						self ! GossipAlgorithm(myID, start_time)
						if(node_with_rumour.length == 0)
						{
							
							var RandomNeighbor:Int = 0
							RandomNeighbor = Random.nextInt(neighbors.length)
							node_with_rumour ::= RandomNeighbor
							node_with_rumour ::= myID
							//println(node_with_rumour.length)
							//println("---------------------------------------------------")
							//println("Transmitting ----" + RandomNeighbor + "----" + myID)
							node(neighbors(RandomNeighbor)) ! GossipAlgorithm(myID, start_time )
							self ! GossipAlgorithm(myID, start_time )
						}
						else{
							var RandomNeighbor:Int = 0
							RandomNeighbor = Random.nextInt(neighbors.length)
							if(!(node_with_rumour contains RandomNeighbor))
							{
									node_with_rumour ::= RandomNeighbor
							}
							var i : Int = 0
							while(i < node_with_rumour.length){
								//println("---------------------------------------------------")
								//println("Transmitting ----" + node_with_rumour(i))
								node(node_with_rumour(i)) ! GossipAlgorithm(myID, start_time )
								i += 1
							}
							
						}

				}*/

				case PushsumAlgorithm(new_s_value:Double, new_w_value:Double, start_time:Long , failureNodes : List[Int] ) => {
					if(!(failureNodes contains myID)){
					//		println("inside pushsum-->negcount="+Delta_Difference)
					//var flag : Int = 1
					//println(myID);
					Count_Rumour += 1
					var oldratio:Double = s_value/w_value
					//		println("oldratio="+oldratio+"s="+s+"w="+w)
					s_value += new_s_value
					w_value += new_w_value
					s_value = s_value/2
					w_value = w_value/2
					var newratio:Double = s_value/w_value
					//		println("newratio="+newratio+"s="+s+"w="+w)
					if ((Count_Rumour == 1) || (Math.abs((oldratio-newratio)) > math.pow(10, -10))) {
								//println("Inside If--->")
								Delta_Difference=0
								var RandomNeighbor = Random.nextInt(neighbors.length)
								node(neighbors(RandomNeighbor)) ! PushsumAlgorithm(s_value, w_value, start_time, failureNodes)
								/*while(flag == 1){

									var RandomNeighbor = Random.nextInt(neighbors.length)
									println("Random" + RandomNeighbor)
									
									if(failureNodes contains RandomNeighbor){
										println("Failure" + RandomNeighbor)
										flag = 1
									}
									else{
										flag = 0
										println("Success" + RandomNeighbor)
										node(neighbors(RandomNeighbor)) ! PushsumAlgorithm(s_value, w_value, start_time, failureNodes)
									}

								}*/
								} 
								else {
								//println("Inside Else---->")
								Delta_Difference += 1
								if (Delta_Difference > 3) {
									println("Sum =" + newratio)
									println("Time = " + (System.currentTimeMillis-start_time) + "ms")
									System.exit(1)
									} else {
										var RandomNeighbor = Random.nextInt(neighbors.length)
										node(neighbors(RandomNeighbor)) ! PushsumAlgorithm(s_value, w_value, start_time, failureNodes)
										/*while(flag == 1){

										var RandomNeighbor = Random.nextInt(neighbors.length)
										if(failureNodes contains RandomNeighbor)
											flag = 1
										else{
											flag = 0
											node(neighbors(RandomNeighbor)) ! PushsumAlgorithm(s_value, w_value, start_time, failureNodes)
										}

								}*/
									}
								}
					}
					else{
						//println("I'm here")
						//println(myID);
					//Count_Rumour += 1
					var oldratio:Double = s_value/w_value
					//		println("oldratio="+oldratio+"s="+s+"w="+w)
					s_value += new_s_value
					w_value += new_w_value
					s_value = s_value/2
					w_value = w_value/2
					var newratio:Double = s_value/w_value
					//		println("newratio="+newratio+"s="+s+"w="+w)
					if ((Count_Rumour == 1) || (Math.abs((oldratio-newratio)) > math.pow(10, -10))) {
								//println("Inside If--->")
								Delta_Difference=0
								var RandomNeighbor = Random.nextInt(neighbors.length)
								node(neighbors(RandomNeighbor)) ! PushsumAlgorithm(s_value, w_value, start_time, failureNodes)
								/*while(flag == 1){

									var RandomNeighbor = Random.nextInt(neighbors.length)
									println("Random" + RandomNeighbor)
									
									if(failureNodes contains RandomNeighbor){
										println("Failure" + RandomNeighbor)
										flag = 1
									}
									else{
										flag = 0
										println("Success" + RandomNeighbor)
										node(neighbors(RandomNeighbor)) ! PushsumAlgorithm(s_value, w_value, start_time, failureNodes)
									}

								}*/
								} 
								else {
								//println("Inside Else---->")
								Delta_Difference += 1
								if (Delta_Difference > 3) {
									println("Sum =" + newratio)
									println("Time = " + (System.currentTimeMillis-start_time) + "ms")
									System.exit(1)
									} else {
										var RandomNeighbor = Random.nextInt(neighbors.length)
										node(neighbors(RandomNeighbor)) ! PushsumAlgorithm(s_value, w_value, start_time, failureNodes)
										/*while(flag == 1){

										var RandomNeighbor = Random.nextInt(neighbors.length)
										if(failureNodes contains RandomNeighbor)
											flag = 1
										else{
											flag = 0
											node(neighbors(RandomNeighbor)) ! PushsumAlgorithm(s_value, w_value, start_time, failureNodes)
										}

								}*/
									}
								}
					}
				}

				case _ => println("Bye")
			}
		}
}



