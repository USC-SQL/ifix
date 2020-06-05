#!/usr/bin/env Rscript

#list.of.packages <- c("ggplot2", "dplyr")
#new.packages <- list.of.packages[!(list.of.packages %in% installed.packages()[,"Package"])]
#if(length(new.packages)) install.packages(new.packages)

library(ggplot2)
library(dplyr)

args = commandArgs(trailingOnly=TRUE)

if (length(args)==0) {
  #stop("You need to pass the path of the folder containing input files.\n", call.=FALSE)
  args[1] = "/home/ifix/Desktop/rentalcars-output"
}

fitnessFilePath <- paste(args[1],"fitness-trend-graph.csv", sep = "/")
fitnessOutputPath <- paste(args[1],"fitness-trend-graph.pdf", sep = "/")
aestheticOutputPath <- paste(args[1],"aesthetic-trend-graph.pdf", sep = "/")
filteredAestheticOutputPath <- paste(args[1],"filtered-aesthetic-trend-graph.pdf", sep = "/")

objectivesFilePath <- paste(args[1],"objectives-graph.csv", sep = "/")
objectivesOutputPath <- paste(args[1],"objectives-graph.pdf", sep = "/")

fitness <- read.table(fitnessFilePath, sep=",", header = TRUE)
fitness <- group_by(fitness, dependentClusterGroup) 
fitness <- mutate(fitness,BestValue = cummin(computedFitnessScore))

cols <- c("minimumValue" = "red", "initialization" = "blue", "mutation" = "darkolivegreen", "crossover" = "orange", "avm" = "skyblue", "best_so_far" = "black")



#-=-=-=-=-=-=-=-=-=-=-=-=-=-=-Fitness Trens Graph=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
fitness.min <- group_by(fitness, dependentClusterGroup) 
fitness.min <- filter(fitness.min,computedFitnessScore == min(computedFitnessScore))
fitness.min <- filter(fitness.min,fitnessCallNoByClusterGroup == min(fitnessCallNoByClusterGroup))
fitness.min$searchStepName = "minimumValue"

fitness.markedmin <- rbind.data.frame(fitness,fitness.min)


fitnessTrendGraph <-ggplot(fitness.markedmin, aes(fitnessCallNoByClusterGroup,computedFitnessScore))+
  geom_point(aes(color = factor(searchStepName)))+
  geom_line(aes(y = BestValue, color="best_so_far"))+
  facet_grid(dependentClusterGroup~.)+
  scale_y_log10()+
  scale_color_manual(values = cols)

ggsave(filename=fitnessOutputPath, plot=fitnessTrendGraph)



#-=-=-=-=-=-=-=-=-=-=-=-=-=-Objectives Graph=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
objectives<-read.table(objectivesFilePath, sep=",", header = TRUE)
ObjectivesGraph <-ggplot(objectives, aes(inconsistenciesObjective,aestheticObjective))+
  scale_x_continuous(trans='log10')+
  geom_point()

ggsave(filename=objectivesOutputPath, plot=ObjectivesGraph)




#-=-=-=-=-=-=-=-=-=-=-=-=-=Aesthetic Graph-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
aestheticTrendGraph <-ggplot(fitness, aes(fitnessCallNoByClusterGroup,computedAestheticObjectiveScore))+
  geom_point(aes(color = factor(searchStepName)))+
  geom_line(aes(y = cummin(computedAestheticObjectiveScore), color="best_so_far"))+
  facet_grid(dependentClusterGroup~.)+
  scale_color_manual(values = cols)

ggsave(filename=aestheticOutputPath, plot=aestheticTrendGraph)


#-=-=-=-=-=-=-=-=-=-=-=-=-=-Filtered Aesthetic Graph=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
fitnessLowestIncon <- group_by(fitness, dependentClusterGroup) 
fitnessLowestIncon <- filter(fitnessLowestIncon,computedInconsistenciesObjectiveScore == min(computedInconsistenciesObjectiveScore))
fitnessLowestIncon <- mutate(fitnessLowestIncon,BestAesthetic = cummin(computedAestheticObjectiveScore))
  
filteredAestheticTrendGraph <-ggplot(fitnessLowestIncon, aes(fitnessCallNoByClusterGroup,computedAestheticObjectiveScore))+
  geom_point(aes(color = factor(searchStepName)))+
  geom_line(aes(y =BestAesthetic, color="best_so_far"))+
  facet_grid(dependentClusterGroup~.)+
  scale_color_manual(values = cols)


ggsave(filename=filteredAestheticOutputPath, plot=filteredAestheticTrendGraph)
