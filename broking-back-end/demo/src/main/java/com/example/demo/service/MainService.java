package com.example.demo.service;

import com.example.demo.domain.Domain;
import com.example.demo.domain.Job;
import com.example.demo.domain.Priority;
import com.example.demo.dto.DomainDTO;
import com.example.demo.dto.JobDTO;
import com.example.demo.repository.DomainRepository;
import com.example.demo.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MainService {

    private final JobRepository jobRepository;

    private final DomainRepository domainRepository;

    public MainService(JobRepository jobRepository, DomainRepository domainRepository) {
        this.jobRepository = jobRepository;
        this.domainRepository = domainRepository;
    }

    public void addDomain(DomainDTO domainDTO){
        Domain newDomain = new Domain();
        newDomain.setAvailableDiskSpace(domainDTO.getAvailableDiskSpace());
        newDomain.setIp(domainDTO.getIp());
        domainRepository.save(newDomain);
    }

    public List<DomainDTO> getDomains(){
        List<DomainDTO> domains = new ArrayList<>();
        for (Domain domain: domainRepository.findAll())
            domains.add(new DomainDTO(domain));

        return domains;
    }

    public Map<String, String> createJob(JobDTO[] jobDTOs){

        Map<String, String> response = new HashMap<>();
        for (JobDTO jobDTO: jobDTOs) {
            Job newJob = new Job();
            newJob.setDiskSpace(jobDTO.getDiskSpace());
            newJob.setNumOfMinutes(jobDTO.getNumOfMinutes());
            newJob.setUserEmail(jobDTO.getUserEmail());
            newJob.setFileName(jobDTO.getFileName());

            jobRepository.save(newJob);

            Domain domain = selectOptimalDomain(jobDTO.getPriority(), jobDTO.getDiskSpace());
            domain.setAvailableDiskSpace( domain.getAvailableDiskSpace() - jobDTO.getDiskSpace() );
            domainRepository.save(domain);

            response.put(jobDTO.getFileName(), domain.getIp());
        }

        return response;
    }

    private Domain selectOptimalDomain(Priority priority, Integer fileSize) {
        List<Domain> domains = domainRepository.findAll();

        Map<Domain, Integer> map = new HashMap<>();

        for (Domain domain: domains){
            try {

                URL url = new URL("http://"+domain.getIp()+":8080/api/load");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                int load = Integer.parseInt(in.readLine());
                //int load = new Random().nextInt(1000);

                map.put(domain, load);
            } catch (IOException e) {
               e.printStackTrace();
           }
}
        List<Map.Entry<Domain,Integer>> sorted = map.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue())
                        .collect(Collectors.toList());

        System.out.println("-----------Domains and Loads-----------");
        for (Map.Entry<Domain, Integer> e: sorted){
            System.out.println(e.getKey().getIp()+" - "+e.getValue());
        }

        switch (priority){
            case LOW:
                if (fileSize<=sorted.get(2).getKey().getAvailableDiskSpace())
                    return sorted.get(2).getKey();
//                else if (fileSize<=sorted.get(1).getKey().getAvailableDiskSpace())
//                    return sorted.get(1).getKey();
//                else if (fileSize<=sorted.get(0).getKey().getAvailableDiskSpace())
//                    return sorted.get(0).getKey();
            case MEDIUM:
                if (fileSize<=sorted.get(1).getKey().getAvailableDiskSpace())
                    return sorted.get(1).getKey();
//                else if (fileSize<=sorted.get(0).getKey().getAvailableDiskSpace())
//                    return sorted.get(0).getKey();
//                else if (fileSize<=sorted.get(2).getKey().getAvailableDiskSpace())
//                    return sorted.get(2).getKey();
            case HIGH:
                if (fileSize<=sorted.get(0).getKey().getAvailableDiskSpace())
                    return sorted.get(0).getKey();
//                else if (fileSize<=sorted.get(1).getKey().getAvailableDiskSpace())
//                    return sorted.get(1).getKey();
//                else if (fileSize<=sorted.get(2).getKey().getAvailableDiskSpace())
//                return sorted.get(2).getKey();
        }

        return null;
   }

}
