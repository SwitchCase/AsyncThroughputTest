for i in {0..100}
do
  curl "localhost:8080/async" &
  pids[${i}]=$!
done

# wait for all pids
for pid in ${pids[*]}; do
    wait $pid
done


