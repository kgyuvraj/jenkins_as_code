import utils.JobUtils
import hudson.model.*
@Grab('org.yaml:snakeyaml:1.17')
import org.yaml.snakeyaml.Yaml

ArrayList searchYamlFiles(String dirPath) {
    ArrayList file_list = []
    File dir = new File(dirPath)
    if (dir.isDirectory()) {
        dir.eachFileRecurse { file ->
            if (file.isFile() && (file.name.endsWith(".yaml") || file.name.endsWith(".yml"))) {
                file_list.add(file.absolutePath)
            }
        }
    } else {
        println("Error: $dirPath is not a valid directory.")
    }
    return file_list
}

// get the current working directory
def cwd = hudson.model.Executor.currentExecutor().getCurrentWorkspace().absolutize()
pipeline_file_list = searchYamlFiles(cwd.toString())

for (current_pipeline in pipeline_file_list) {

    println("Working on :" + current_pipeline)

    JobUtils job_config = new JobUtils(current_pipeline)
    println("job name is : "+job_config.get_job_name())
    pipelineJob(job_config.get_job_name()){
        definition {
            cps {
                script("""
pipeline {
        agent any

        stages {

            stage('Checkout') {
                steps {
                    echo 'Checking out source code...'
                }
            }

            stage('Build') {
                steps {
                    echo '${job_config.get_build_command()}'
                }
            }

        }
    }
""")
                sandbox()
            }
        }
    }
}